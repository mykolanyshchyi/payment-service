package com.payment.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.Servers;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Payment Service API", version = "v1", contact = @Contact(name = "Mykola Nyshchyi", email = "mykola.nyshchyi@gmail.com")))
@Servers(value = {
        @Server(url = "https://payment.service")
})
public class SpringDocConfiguration {
}
