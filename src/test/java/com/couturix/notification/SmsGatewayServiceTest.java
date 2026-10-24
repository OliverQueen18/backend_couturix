package com.couturix.notification;

import com.couturix.config.CouturixProperties;
import com.couturix.domain.enums.PlanCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmsGatewayServiceTest {

    private final SmsGatewayService service = new SmsGatewayService(
            new ObjectMapper(),
            new CouturixProperties(
                    null,
                    null,
                    null,
                    new CouturixProperties.Notifications(
                            "noreply@couturix.test",
                            new CouturixProperties.Notifications.Whatsapp(false, "", "", ""),
                            new CouturixProperties.Notifications.Sms(
                                    false, "", "https://example.invalid", 10, "+223")),
                    null));

    @Test
    void normaliseHuitChiffresMaliens() {
        assertEquals("+22376123456", service.normalizePhoneNumber("76 12 34 56"));
        assertEquals("+22376123456", service.normalizePhoneNumber("0022376123456"));
        assertEquals("+22376123456", service.normalizePhoneNumber("+223 76 12 34 56"));
    }

    @Test
    void refuseNumeroInvalide() {
        assertNull(service.normalizePhoneNumber("12"));
        assertNull(service.normalizePhoneNumber(""));
        assertNull(service.normalizePhoneNumber(null));
    }

    @Test
    void gatewayPasPretSansCle() {
        assertFalse(service.isReady());
    }

    @Test
    void smsAPartirDeStandard() {
        assertFalse(PlanCode.GRATUIT.autoriseSms());
        assertTrue(PlanCode.STANDARD.autoriseSms());
        assertTrue(PlanCode.PREMIUM.autoriseSms());
        assertTrue(PlanCode.GOLD.autoriseSms());
    }
}
