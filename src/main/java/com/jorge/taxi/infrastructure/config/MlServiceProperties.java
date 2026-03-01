package com.jorge.taxi.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración del servicio de Machine Learning (ML) para predicciones.
 * 
 * <p>Este componente carga las propiedades desde <code>application.yml</code> o
 * <code>application.properties</code> usando el prefijo <b>ml.service</b>.
 * Por ejemplo:</p>
 * 
 * <pre>
 * ml.service.url=http://localhost:8000/predict
 * </pre>
 * 
 * <p>Si no se define ninguna URL, se utilizará por defecto:
 * <code>http://localhost:8000/predict</code>.</p>
 * 
 * <p>Se utiliza en {@link com.jorge.taxi.infrastructure.adapter.out.ml.MlHttpClient}
 * para realizar las llamadas HTTP al servicio ML.</p>
 * 
 * @author Jorge Campos Rodríguez
 * @version 1.0.2
 */
@Component
@ConfigurationProperties(prefix = "ml.service")
public class MlServiceProperties {

    /**
     * URL del servicio ML para predicciones.
     */
    private String url = "http://localhost:8000/predict"; // valor por defecto

    
    /**
     * Constructor por defecto para ML.
     */
    public MlServiceProperties() {
    }

    
    /**
     * Devuelve la URL del microservicio de ML.
     * @return url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Setea la URL del microservicio de ML.
     * @param url microservicio de Machine Learning
     */
    public void setUrl(String url) {
        this.url = url;
    }
}