package com.example.routeauthorizationtesting

import com.example.routeauthorizationtesting.Access.AnyRole
import com.example.routeauthorizationtesting.Access.Unauthenticated
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.GET

typealias AuthorizationSpecs = Set<AuthorizationSpec>

@Configuration
class ConfigureAuthorizationSpecs {

    @Bean
    fun configure(): AuthorizationSpecs = setOf(
        AuthorizationSpec("/user/{id}", GET, AnyRole(Role.ADMIN)),
        AuthorizationSpec("/actuator/info", GET, Unauthenticated),
        AuthorizationSpec("/actuator/health", GET, Unauthenticated)
    )
}
