package com.expensetracker.config;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnvironmentConfigurationTest {
    @Configuration(proxyBeanMethods = false)
    static class ConfigOnly {}

    private StandardEnvironment load(String profile, Map<String, Object> values) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().addFirst(new MapPropertySource("test-input", values));
        if (profile != null) environment.setActiveProfiles(profile);
        try (var context = new SpringApplicationBuilder(ConfigOnly.class)
                .environment(environment).web(WebApplicationType.NONE)
                .properties("spring.main.banner-mode=off").run()) {
            return environment;
        }
    }

    @Test
    void devSuppliesLocalDefaultsButRequiresSecrets() {
        var env = load("dev", Map.of());
        assertThat(env.getProperty("spring.datasource.url")).isEqualTo("jdbc:postgresql://localhost:5432/expensetracker");
        assertThat(env.getProperty("spring.datasource.username")).isEqualTo("postgres");
        assertThat(env.getProperty("app.cors.allowed-origin")).isEqualTo("http://localhost:5173");
        assertThat(env.getProperty("spring.jpa.show-sql")).isEqualTo("true");
        assertThatThrownBy(() -> env.getProperty("spring.datasource.password")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> env.getProperty("jwt.secret")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void prodAndNoProfileRequireExplicitValues() {
        for (String profile : new String[] {"prod", null}) {
            var env = load(profile, Map.of());
            assertThat(env.getProperty("spring.jpa.show-sql")).isEqualTo("false");
            assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
            assertThat(env.getProperty("jwt.expiration")).isEqualTo("604800000");
            for (String property : new String[] {"spring.datasource.url", "spring.datasource.username",
                    "spring.datasource.password", "jwt.secret", "app.cors.allowed-origin"}) {
                assertThatThrownBy(() -> env.getProperty(property)).isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

    @Test
    void environmentOverridesWorkForDevAndProd() {
        for (String profile : new String[] {"dev", "prod"}) {
            var env = load(profile, Map.of("DB_URL", "jdbc:postgresql://db.example/test",
                    "DB_USERNAME", "app", "DB_PASSWORD", "test-password",
                    "JWT_SECRET", "test-secret", "CORS_ALLOWED_ORIGIN", "https://expenses.example.com"));
            assertThat(env.getProperty("spring.datasource.url")).isEqualTo("jdbc:postgresql://db.example/test");
            assertThat(env.getProperty("spring.datasource.username")).isEqualTo("app");
            assertThat(env.getProperty("spring.datasource.password")).isEqualTo("test-password");
            assertThat(env.getProperty("jwt.secret")).isEqualTo("test-secret");
            assertThat(env.getProperty("app.cors.allowed-origin")).isEqualTo("https://expenses.example.com");
        }
    }

    @Test
    void corsUsesConfiguredOriginAndPreservesMethodsAndHeaders() {
        var source = new SecurityConfig("https://expenses.example.com").corsConfigurationSource();
        var cors = source.getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/api/expenses"));
        assertThat(cors).isNotNull();
        assertThat(cors.checkOrigin("https://expenses.example.com")).isEqualTo("https://expenses.example.com");
        assertThat(cors.checkOrigin("http://localhost:5173")).isNull();
        assertThat(cors.getAllowedMethods()).containsExactly("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        assertThat(cors.getAllowedHeaders()).containsExactly("Authorization", "Content-Type", "Accept");
    }
}
