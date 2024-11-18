package com.quest.etna.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultController {
	
    @GetMapping("/testSuccess")
    public ResponseEntity<String> testSuccess() {
        return new ResponseEntity<>("success", HttpStatus.OK); // HTTP 200
    }
	
    @GetMapping("/testNotFound")
    public ResponseEntity<String> testNotFound() {
        return new ResponseEntity<>("not found", HttpStatus.NOT_FOUND); // HTTP 404
    }
	
    @GetMapping("/testError")
    public ResponseEntity<String> testError() {
        return new ResponseEntity<>("error", HttpStatus.INTERNAL_SERVER_ERROR); // HTTP 500
    }
}
