package com.jorge.taxi.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración de clientes HTTP de la aplicación.
 * 
 * <p>Proporciona un {@link RestTemplate} como bean de Spring
 * que puede ser inyectado en cualquier componente que necesite
 * hacer llamadas HTTP (por ejemplo, {@link com.jorge.taxi.infrastructure.client.MlHttpClient}).</p>
 *
 * <p>Al centralizar la creación de {@code RestTemplate} en un bean,
 * se facilita la personalización futura, como añadir interceptores,
 * timeouts o logging de peticiones.</p>
 * 
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 * @see RestTemplate
 */
@Configuration
public class HttpClientConfig {

    /**
     * Crea un bean de {@link RestTemplate} para inyección en componentes.
     *
     * @return un RestTemplate configurado por defecto
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}