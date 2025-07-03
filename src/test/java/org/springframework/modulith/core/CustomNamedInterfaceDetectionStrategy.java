package org.springframework.modulith.core;

import com.tngtech.archunit.core.domain.JavaClass;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

// we need to resort to place this class inside the org.springframework.modulith.core package
// because while `public static NamedInterface of(String name, Classes classes)` is public,
// it's parameter `Classes` is currently package private
// Also `NamedInterfaces.of(List<NamedInterface> namedInterfaces)` is package private

public class CustomNamedInterfaceDetectionStrategy implements ApplicationModuleDetectionStrategy {

    @Override
    public Stream<JavaPackage> getModuleBasePackages(JavaPackage rootPackage) {
        return ApplicationModuleDetectionStrategy.explicitlyAnnotated().getModuleBasePackages(rootPackage);
    }

    @Override
    public NamedInterfaces detectNamedInterfaces(JavaPackage basePackage, ApplicationModuleInformation information) {
        var namedInterfaces = NamedInterfaces.discoverNamedInterfaces(basePackage);
        var newNamedInterfaces = namedInterfaces.stream().map(nameInterface -> {
            var exposedTypes = nameInterface.asJavaClasses()
                    .flatMap(javaClass -> methodSignatureExposedTypes(basePackage, javaClass).stream())
                    .distinct().toList();
            return NamedInterface.of(nameInterface.getName(), Classes.of(exposedTypes));
        }).toList();
        return NamedInterfaces.of(newNamedInterfaces);
    }

    private static List<JavaClass> methodSignatureExposedTypes(JavaPackage basePackage, JavaClass javaClass) {
        var dependencies = new HashSet<JavaClass>();
        var visited = new HashSet<JavaClass>();

        dependencies.add(javaClass);
        collectExposedTypes(javaClass, dependencies, visited);

        return dependencies.stream()
                .filter(basePackage::contains)
                .toList();
    }

    private static void collectExposedTypes(JavaClass type, Set<JavaClass> exposedTypes, Set<JavaClass> visited) {
        if (visited.contains(type)) {
            return;
        }

        visited.add(type);

        type.getAllMethods().forEach(method -> {
            method.getParameterTypes().forEach(paramType -> {
                Set<JavaClass> rawTypes = paramType.getAllInvolvedRawTypes();
                exposedTypes.addAll(rawTypes);

                rawTypes.forEach(rawType -> {
                    collectExposedTypes(rawType, exposedTypes, visited);
                });
            });

            Set<JavaClass> returnRawTypes = method.getReturnType().getAllInvolvedRawTypes();
            exposedTypes.addAll(returnRawTypes);

            returnRawTypes.forEach(rawType -> {
                collectExposedTypes(rawType, exposedTypes, visited);
            });
        });
    }

}
