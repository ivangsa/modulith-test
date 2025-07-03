@org.springframework.modulith.ApplicationModule(
        id = "moduleC",
        displayName = "Module C",
        allowedDependencies = { "moduleA", "moduleA::*" }
)package com.example.modulithtest.moduleC;
