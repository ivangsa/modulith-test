package com.example.modulithtest.moduleC;

import com.example.modulithtest.moduleA.events.AnEvent;
import org.springframework.modulith.events.ApplicationModuleListener;

public class MyListener {

    @ApplicationModuleListener
    public void handleEvent(AnEvent event) {
        System.out.println("Received event: " + event);
    }
}
