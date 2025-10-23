package com.event.eventscheduler.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {


    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RecurringEventScheduleAPI")
                        .version("1.0.0")
                        .description("A Restful API for Event Scheduling for teams.")
                        .contact(new Contact()
                                .name("Emre Tokluk")
                                .email("emretokluk@gmail.com")
                                .url("https://www.emretokluk.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));

    }
}
