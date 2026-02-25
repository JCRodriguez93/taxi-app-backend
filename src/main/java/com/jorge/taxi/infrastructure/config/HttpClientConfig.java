package com.jorge.taxi.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración de clientes HTTP de la aplicación.
 *
 * <p>Define un {@link RestTemplate} configurado con tiempos máximos
 * de conexión y lectura para evitar bloqueos indefinidos al
 * comunicarse con servicios externos.</p>
 *
 * <p>Permite centralizar configuraciones como timeouts,
 * interceptores o mecanismos de resiliencia.</p>
 *
 * @author Jorge Campos Rodríguez
 * @version 1.0.1
 */
@Configuration
public class HttpClientConfig {

    /**
     * Crea un {@link RestTemplate} configurado con timeouts.
     *
     * <ul>
     *   <li>Connect timeout: tiempo máximo para establecer conexión.</li>
     *   <li>Read timeout: tiempo máximo esperando respuesta.</li>
     * </ul>
     *
     * @param builder builder proporcionado por Spring Boot
     * @return RestTemplate configurado con timeouts
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {

        return builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }
}