package com.example.web;

import com.example.model.Order;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public interface OrdersApi {
    @PostMapping(
            value = "/orders",
            consumes = "application/json"
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void createOrder(@Valid @RequestBody(required = false) Order requestBody);
}
