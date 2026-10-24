package com.couturix.notification;

import com.couturix.config.CouturixProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Client de l’API V2 Android SMS Gateway (SimpApp), même passerelle qu’eRDV, PIE Mali et Tontine.
 * Les erreurs SMS sont journalisées sans interrompre le métier.
 */
@Slf4j
@Service
public class SmsGatewayService {

    private final ObjectMapper objectMapper;
    private final CouturixProperties properties;
    private final HttpClient httpClient;

    public SmsGatewayService(ObjectMapper objectMapper, CouturixProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public boolean isReady() {
        return sms().ready();
    }

    public boolean sendSms(String phoneNumber, String message) {
        CouturixProperties.Notifications.Sms sms = sms();
        if (!sms.enabled()) {
            log.info("SMS Gateway désactivé — message non envoyé à {}", maskPhone(phoneNumber));
            return false;
        }
        if (phoneNumber == null || phoneNumber.isBlank() || message == null || message.isBlank()) {
            return false;
        }
        if (sms.apiKey() == null || sms.apiKey().isBlank()) {
            log.warn("SMS Gateway activé mais SMS_GATEWAY_API_KEY n’est pas configurée.");
            return false;
        }

        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        if (normalizedPhone == null) {
            log.warn("Numéro SMS invalide (format E.164 requis): {}", phoneNumber);
            return false;
        }

        String bodyText = message.trim();
        if (bodyText.length() > 160) {
            bodyText = bodyText.substring(0, 157) + "...";
        }

        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "phoneNumber", normalizedPhone,
                    "message", bodyText
            ));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sms.url()))
                    .timeout(Duration.ofSeconds(Math.max(1, sms.requestTimeoutSeconds())))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + sms.apiKey().trim())
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Échec SMS Gateway HTTP {} pour {} — body: {}",
                        response.statusCode(), maskPhone(normalizedPhone), truncate(response.body()));
                return false;
            }
            JsonNode body = objectMapper.readTree(response.body());
            boolean success = isAcceptedResponse(body);
            if (!success) {
                log.warn("SMS Gateway a refusé le message pour {} — body: {}",
                        maskPhone(normalizedPhone), truncate(response.body()));
            } else {
                log.info("SMS mis en file / envoyé à {}", maskPhone(normalizedPhone));
            }
            return success;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Envoi SMS interrompu pour {}", maskPhone(normalizedPhone));
            return false;
        } catch (Exception ex) {
            log.warn("Erreur SMS Gateway pour {}: {}", maskPhone(normalizedPhone), ex.getMessage());
            return false;
        }
    }

    public String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }
        String normalized = phoneNumber.trim().replaceAll("[\\s().-]", "");
        if (normalized.startsWith("00")) {
            normalized = "+" + normalized.substring(2);
        }
        if (normalized.matches("\\d{8}")) {
            String cc = sms().defaultCountryCode();
            if (cc == null || cc.isBlank()) {
                cc = "+223";
            }
            cc = cc.trim();
            if (!cc.startsWith("+")) {
                cc = "+" + cc;
            }
            normalized = cc + normalized;
        } else if (!normalized.startsWith("+") && normalized.matches("^\\d{8,15}$")) {
            if (normalized.startsWith("223") && normalized.length() == 11) {
                normalized = "+" + normalized;
            }
        }
        if (!normalized.matches("^\\+[1-9]\\d{7,14}$")) {
            return null;
        }
        return normalized;
    }

    private boolean isAcceptedResponse(JsonNode body) {
        if (body == null || body.isMissingNode() || body.isNull()) {
            return false;
        }
        if (body.path("success").asBoolean(false)) {
            return true;
        }
        String status = body.path("status").asText("").trim().toLowerCase();
        if (status.equals("queued")
                || status.equals("sent")
                || status.equals("delivered")
                || status.equals("accepted")
                || status.equals("ok")
                || status.equals("success")) {
            return true;
        }
        return body.hasNonNull("smsId") && !body.path("smsId").asText("").isBlank();
    }

    private CouturixProperties.Notifications.Sms sms() {
        CouturixProperties.Notifications notifications = properties.notifications();
        if (notifications != null && notifications.sms() != null) {
            return notifications.sms();
        }
        return new CouturixProperties.Notifications.Sms(false, "", "", 10, "+223");
    }

    private String truncate(String body) {
        if (body == null) {
            return "";
        }
        return body.length() > 200 ? body.substring(0, 200) + "…" : body;
    }

    private String maskPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 5) {
            return "***";
        }
        return phoneNumber.substring(0, 4) + "****" + phoneNumber.substring(phoneNumber.length() - 2);
    }
}
