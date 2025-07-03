# SpringModulith Modules Validation Playground

> Note: this will be addressed in SpringModulith 2.0.0 scheduled for around November 2025.
> Until then you can use a custom ApplicationModuleDetectionStrategy, see an example bellow.

Up to SpringModulith v1.4.1, to use components across different modules (and validate them), it’s not sufficient to place exported services in the **module’s root package** or annotate them with `@NamedInterface`.

You must also either:
- Annotate also **all exposed types** (parameters and return types, including transitive types), or
- Place all related classes in the **module’s root package**

**This creates a terrible DX!**

Exporting an interface should suffice to express a clear intention without needing to annotate every single type. You either export an interface or you don't.

## Playground Project showcasing this issue

There are 3 modules in this project:
- Module A exposes:
  - One named Event
  - One named Service
  - One Root Package Service

![Project Screenshot](./project-screenshot.png)

- Modules B and C both depend on Module A:

![Module B and C depend on Module A](./modules-screenshot.png)

But module setup can not be validated because there are related types that are not, requiring either:

- explicitly annotated (MyEnum, MyRelatedDTO, AnEnum, RelatedType,...)
- present on the root package of the module

Even if all main types are intentionaly exported..

```log
org.springframework.modulith.core.Violations: 

- Module 'moduleB' depends on named interface(s) 'moduleA :: events.AnEvent' via com.example.modulithtest.moduleB.MyListener -> com.example.modulithtest.moduleA.events.AnEvent. Allowed targets: moduleA.
- Module 'moduleB' depends on module 'moduleA' via com.example.modulithtest.moduleB.MyService -> com.example.modulithtest.moduleA.dtos.MyEnum. Allowed targets: moduleA.
- Module 'moduleB' depends on module 'moduleA' via com.example.modulithtest.moduleB.MyService -> com.example.modulithtest.moduleA.dtos.MyRelatedDTO. Allowed targets: moduleA.
- Module 'moduleB' depends on module 'moduleA' via com.example.modulithtest.moduleB.MyService -> com.example.modulithtest.moduleA.dtos.MyDTO. Allowed targets: moduleA.

- Module 'moduleC' depends on named interface(s) 'moduleA :: events.AnEvent' via com.example.modulithtest.moduleC.MyListener -> com.example.modulithtest.moduleA.events.AnEvent. Allowed targets: moduleA.
- Module 'moduleC' depends on module 'moduleA' via com.example.modulithtest.moduleC.MyService -> com.example.modulithtest.moduleA.dtos.MyEnum. Allowed targets: moduleA.
- Module 'moduleC' depends on named interface(s) 'moduleA :: ANamedService' via com.example.modulithtest.moduleC.MyService -> com.example.modulithtest.moduleA.services.ANamedService. Allowed targets: moduleA.
- Module 'moduleC' depends on module 'moduleA' via com.example.modulithtest.moduleC.MyService -> com.example.modulithtest.moduleA.dtos.MyDTO. Allowed targets: moduleA.
- Module 'moduleC' depends on module 'moduleA' via com.example.modulithtest.moduleC.MyService -> com.example.modulithtest.moduleA.dtos.MyRelatedDTO. Allowed targets: moduleA.
```

If this is the intended behaviour is a terrible user experience.

## Using a Custom `ApplicationModuleDetectionStrategy`

You can create a custom `ApplicationModuleDetectionStrategy` that detects the transitive dependencies of the named interfaces, and use it by setting the `spring.modulith.detection-strategy` property to the fully qualified name of your custom strategy.

```java
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
      ...
    }
}
```

And use it in your tests:

```java
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
```
