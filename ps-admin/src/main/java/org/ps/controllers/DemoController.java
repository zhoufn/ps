package org.ps.controllers;

import org.ps.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DemoController {

    @Autowired
    private TaskService taskService;

    @RequestMapping("/demo")
    public String demo(){
        this.taskService.insertWaitingTask();
        return "hello world!";
    }


}
