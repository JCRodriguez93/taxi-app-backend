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
 * <p>Se utiliza en {@link com.jorge.taxi.infrastructure.adapter.out.ml.model.MlHttpClient}
 * para realizar las llamadas HTTP al servicio ML.</p>
 * 
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "ml.service")
public class MlServiceProperties {

    /**
     * URL del servicio ML para predicciones.
     */
    private String url = "http://localhost:8000/predict"; // valor por defecto

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}