package com.expensetracker.config;

import com.expensetracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

// Probe endpoints isolate the real security chain without a database or health contributors.
@WebMvcTest(controllers = HealthSecurityTest.ProbeController.class, properties = {
    "jwt.secret=VGhpc0lzQVRlc3RTZWNyZXRLZXlGb3JKV1RUZXN0aW5nMTIzNDU2Nzg5MA==",
    "app.cors.allowed-origin=http://localhost:5173"
})
@Import({SecurityConfig.class, HealthSecurityTest.ProbeController.class, HealthSecurityTest.WebSecurity.class})
@EnableConfigurationProperties(JwtProperties.class)
class HealthSecurityTest {
    @org.springframework.boot.test.context.TestConfiguration(proxyBeanMethods = false)
    @org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
    static class WebSecurity {}

    @RestController
    static class ProbeController {
        @GetMapping({"/actuator/health", "/actuator/health/readiness", "/actuator/info", "/actuator", "/api/expenses"})
        String probe() { return "ok"; }
    }

    @Autowired MockMvc mvc;
    @MockitoBean UserRepository users;

    @Test
    void exactHealthPathIsPublic() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void otherActuatorAndApplicationPathsStillRequireAuthentication() throws Exception {
        for (String path : new String[]{"/actuator", "/actuator/info", "/actuator/health/readiness", "/api/expenses"}) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
            mvc.perform(get(path).with(jwt())).andExpect(status().isOk());
        }
    }

    @Test
    void invalidTokenIsRejectedEvenOnPublicHealthPath() throws Exception {
        mvc.perform(get("/actuator/health").header("Authorization", "Bearer invalid-token"))
            .andExpect(status().isUnauthorized());
    }
}
