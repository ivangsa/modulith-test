@org.springframework.modulith.ApplicationModule(
        id = "moduleB",
        displayName = "Module B",
        allowedDependencies = { "moduleA", "moduleA::*" }
)
package com.example.modulithtest.moduleB;
