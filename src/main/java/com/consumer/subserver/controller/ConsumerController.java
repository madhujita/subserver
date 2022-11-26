package com.consumer.subserver.controller;

import com.consumer.subserver.service.ConsumerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subserver/")
public class ConsumerController {

    private ConsumerService consumerService;

    @GetMapping("/health")
    public ResponseEntity<String> isHealthy(){
        return ResponseEntity.status(HttpStatus.OK).body("Service is healthy!\n");
    }

    @GetMapping("/messages")
    public ResponseEntity<String> getMessages(){
        return null;
    }
}
