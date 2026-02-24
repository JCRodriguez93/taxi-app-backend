package com.jorge.taxi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AppBackendApplicationTest {

    @Test
    @DisplayName("Debería arrancar la aplicación (invocar main)")
    void main_shouldRun() {
        String[] args = {};
        AppBackendApplication.main(args);
    }
}