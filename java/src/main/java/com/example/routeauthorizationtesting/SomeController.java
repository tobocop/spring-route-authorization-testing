package com.example.routeauthorizationtesting;

import org.springframework.web.bind.annotation.*;

@RestController
public class SomeController {
    @GetMapping("/user/{id}")
    String getUser(@PathVariable String id) { return "getUser " + id; }

    @PostMapping("/user")
    String postUser() { return "postUser";}

    @PutMapping("/user/{id}")
    String putUser(@PathVariable String id) { return "putUser " + id; }

    @PatchMapping("/user/{id}")
    String patchUser(@PathVariable String id) { return "patchUser " + id; }

    @DeleteMapping("/user/{id}")
    String deleteUser(@PathVariable String id) { return "deleteUser " + id; }

    @GetMapping("/some-untested-route-to-show-failure")
    String untested() { return "nope";}

    @PostMapping("/some-route-without-correct-auth-to-show-failure")
    String wrongAuth() { return "nope";}
}
