package com.example.modulithtest;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class ModulithTest {

    ApplicationModules modules = ApplicationModules.of(ModulithTestApplication.class);

    @Test
    void verifyModules() {
        modules.verify();
    }

}
