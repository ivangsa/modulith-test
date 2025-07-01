package com.example.modulithtest.moduleA.events;

import org.springframework.modulith.NamedInterface;

@NamedInterface(name = "events.AnEvent")
public record AnEvent(RelatedType relatedType, AnEnum anEnum) {
}
