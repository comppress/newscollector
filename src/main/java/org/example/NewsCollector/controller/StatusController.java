package org.example.NewsCollector.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    public class Status {
        public String status = "NewsCollector Up";
    }

    @GetMapping("/status")
    public Status getStatus(){
        return new Status();
    }

}
