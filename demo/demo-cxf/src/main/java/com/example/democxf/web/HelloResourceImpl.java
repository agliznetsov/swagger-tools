package com.example.democxf.web;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HelloResourceImpl implements HelloResource {

    @Override
    public List<Hello> hello(String name, Integer count, Hello body) {
        List<Hello> list = new ArrayList<>();
        for(int i=0; i<count; i++) {
            Hello res = new Hello();
            res.setMsg(body.getMsg() + " " + name);
            list.add(res);
        }
        return list;
    }

}
