package com.example.routeauthorizationtesting

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class SomeController {
    @GetMapping("/user/{id}")
    fun getUser(@PathVariable id: String): String {
        return "some user with id: $id"
    }
}
