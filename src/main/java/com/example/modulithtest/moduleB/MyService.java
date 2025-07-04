package com.example.modulithtest.moduleB;

import com.example.modulithtest.moduleA.APublicService;
import com.example.modulithtest.moduleA.dtos.MyDTO;
import com.example.modulithtest.moduleA.dtos.MyEnum;
import com.example.modulithtest.moduleA.dtos.MyRelatedDTO;
import com.example.modulithtest.moduleA.services.ANamedService;

public class MyService {
    private APublicService aPublicService;
    private ANamedService aNamedService;

    public void doSomething() {
        var myDTO = new MyDTO("name", new MyRelatedDTO("name"), MyEnum.ONE);
        aPublicService.doSomething(myDTO);
        aNamedService.doSomething(myDTO);
    }
}
