package com.example.routeauthorizationtesting

import com.example.routeauthorizationtesting.Access.AnyRole
import com.example.routeauthorizationtesting.Access.Unauthenticated
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.*

typealias RouteAuthSpecs = Set<RouteAuthSpec>

@Configuration
class ConfigureRouteAuthSpecs {
    @Bean
    fun configure(): RouteAuthSpecs = setOf(
        RouteAuthSpec("/user/{id}", GET, AnyRole(Role.ADMIN, Role.BASIC)),
        RouteAuthSpec("/user/{id}", PUT, AnyRole(Role.ADMIN, Role.BASIC)),
        RouteAuthSpec("/user/{id}", PATCH, AnyRole(Role.ADMIN, Role.BASIC)),
        RouteAuthSpec("/user", POST, AnyRole(Role.ADMIN)),
        RouteAuthSpec("/user/{id}", DELETE, AnyRole(Role.ADMIN)),
        RouteAuthSpec("/some-route-without-correct-auth-to-show-failure", POST, AnyRole(Role.ADMIN)),
        RouteAuthSpec("/actuator/info", GET, Unauthenticated),
        RouteAuthSpec("/actuator/health", GET, Unauthenticated)
    )
}
