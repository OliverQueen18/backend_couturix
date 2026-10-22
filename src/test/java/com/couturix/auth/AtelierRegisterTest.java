package com.couturix.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AtelierRegisterTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void inscriptionCreeAtelierEtCompteAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register-atelier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "atelierNom": "Maison Ciseaux",
                                  "rccm": "CG-BZV-2026-A-001",
                                  "nif": "A1234567B",
                                  "ville": "Brazzaville",
                                  "email": "gerant-maison@couturix.test",
                                  "password": "Atelier123!",
                                  "nom": "Kaya",
                                  "prenom": "Amina"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.atelierNom").value("Maison Ciseaux"))
                .andExpect(jsonPath("$.emailVerified").value(false))
                .andExpect(jsonPath("$.roles", org.hamcrest.Matchers.hasItem("ADMIN")));
    }
}
