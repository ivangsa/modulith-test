package com.example.modulithtest;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.core.CustomNamedInterfaceDetectionStrategy;
import org.springframework.modulith.docs.Documenter;

public class ModulithTest {

    ApplicationModules modules = ApplicationModules.of(ModulithTestApplication.class);

    @Test
    void verifyModules() {
        System.setProperty("spring.modulith.detection-strategy", "");
        // this test fails to validate the module dependencies bc with default detection strategy,
        // it does not detect the transitive dependencies of the named interfaces,
        // so you either need to explicitly annotate all types, or place them flat in the root package
        modules.verify();
    }

    @Test
    void verifyModules_withCustomDetectionStrategy() {
        System.setProperty("spring.modulith.detection-strategy", CustomNamedInterfaceDetectionStrategy.class.getName());
        // with this custom detection strategy, the transitive dependencies of the named interfaces are detected,
        // and the module dependencies are correctly validated
        // you only need to annotate the main types: service interfaces and domain events.
        modules.verify();
    }

}
