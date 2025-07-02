# SpringModulith Modules Validation Playground

Up to SpringModulith v1.4.1, to use components across different modules (and validate them), it’s not sufficient to place exported services in the **module’s root package** or annotate them with `@NamedInterface`.

You must also either:
- Annotate also **all exposed types** (parameters and return types, including transitive types), or
- Place all related classes in the **module’s root package**

**This creates a terrible DX!**

Exporting an interface should suffice to express a clear intention without needing to annotate every single type. You either export an interface or you don't.

And it's not clear if this is a bug or the intended behaviour. See
https://github.com/spring-projects/spring-modulith/issues/1264

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
