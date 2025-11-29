package com.authflow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration.
 * 
 * <p>
 * Access Swagger UI at: http://localhost:8081/swagger-ui.html
 * </p>
 * <p>
 * Access API Docs at: http://localhost:8081/v3/api-docs
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AuthFlow API")
                        .description("Complete Authentication & Authorization Demo API\n\n" +
                                "This API demonstrates:\n" +
                                "- JWT Authentication\n" +
                                "- Role-Based Access Control (RBAC)\n" +
                                "- Permission-Based Authorization\n" +
                                "- Password Reset Flow\n" +
                                "- Email Notifications\n" +
                                "- Caching (LRU, LFU, TTL)\n" +
                                "- Security Best Practices")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Shivam Srivastav")
                                .email("shivamsriv961@gmail.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.authflow.com")
                                .description("Production Server (Example)")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from /api/auth/login")));
    }
}
