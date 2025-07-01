package com.example.modulithtest.moduleA.services;

import com.example.modulithtest.moduleA.dtos.MyDTO;
import org.springframework.modulith.NamedInterface;

@NamedInterface(name = "ANamedService")
public interface ANamedService {

    void doSomething(MyDTO dto);
}
