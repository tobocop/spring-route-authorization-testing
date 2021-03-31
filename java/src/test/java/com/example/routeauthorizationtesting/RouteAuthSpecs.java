package com.example.routeauthorizationtesting;

import com.example.routeauthorizationtesting.Access.AnyRole;
import com.example.routeauthorizationtesting.Access.Unauthenticated;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

import static org.springframework.http.HttpMethod.*;

@Configuration
class ConfigureRouteAuthSpecs {
    @Bean
    Set<RouteAuthSpec> configure() {
        return Set.of(
            new RouteAuthSpec("/user/{id}", GET, new AnyRole(Role.ADMIN, Role.BASIC)),
            new RouteAuthSpec("/user/{id}", PUT, new AnyRole(Role.ADMIN, Role.BASIC)),
            new RouteAuthSpec("/user/{id}", PATCH, new AnyRole(Role.ADMIN, Role.BASIC)),
            new RouteAuthSpec("/user", POST, new AnyRole(Role.ADMIN)),
            new RouteAuthSpec("/user/{id}", DELETE, new AnyRole(Role.ADMIN)),
            new RouteAuthSpec("/some-route-without-correct-auth-to-show-failure", POST, new AnyRole(Role.ADMIN))
        );
    }
}
