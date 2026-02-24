package com.jorge.taxi.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MlServicePropertiesTest {

    @Test
    @DisplayName("Debería devolver la URL por defecto y permitir cambiarla")
    void urlGetterSetter_shouldWork() {
        MlServiceProperties properties = new MlServiceProperties();

        // URL por defecto
        assertEquals("http://localhost:8000/predict", properties.getUrl());

        // cambiar URL
        properties.setUrl("http://ml-server:9000/predict");
        assertEquals("http://ml-server:9000/predict", properties.getUrl());
    }
}