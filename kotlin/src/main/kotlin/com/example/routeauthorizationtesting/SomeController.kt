package com.example.routeauthorizationtesting

import org.springframework.web.bind.annotation.*

@RestController
class SomeController {
    @GetMapping("/user/{id}")
    fun getUser(@PathVariable id: String): String = "getUser $id"

    @PostMapping("/user")
    fun postUser(): String = "postUser"

    @PutMapping("/user/{id}")
    fun putUser(@PathVariable id: String): String = "putUser $id"

    @PatchMapping("/user/{id}")
    fun patchUser(@PathVariable id: String): String = "patchUser $id"

    @DeleteMapping("/user/{id}")
    fun deleteUser(@PathVariable id: String): String = "deleteUser $id"


    @GetMapping("/some-untested-route-to-show-failure")
    fun untested(): String = "nope"

    @PostMapping("/some-route-without-correct-auth-to-show-failure")
    fun wrongAuth(): String = "nope"
}
