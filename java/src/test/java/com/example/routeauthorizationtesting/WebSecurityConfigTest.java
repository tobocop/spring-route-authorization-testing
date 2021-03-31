package com.example.routeauthorizationtesting;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
//@AutoConfigureMockMvc
class WebSecurityConfigTest {
    final Set<RouteAuthSpec> routeAuthSpecs = Set.of(
        new RouteAuthSpec("/user/{id}", HttpMethod.GET, new Access.Unauthenticated()),
        new RouteAuthSpec("/user/{id}", HttpMethod.PUT, new Access.AnyRole(Role.ADMIN, Role.BASIC)),
        new RouteAuthSpec("/user/{id}", HttpMethod.PATCH, new Access.AnyRole(Role.ADMIN, Role.BASIC)),
        new RouteAuthSpec("/user", HttpMethod.POST, new Access.AnyRole(Role.ADMIN)),
        new RouteAuthSpec("/user/{id}", HttpMethod.DELETE, new Access.AnyRole(Role.ADMIN)),
        new RouteAuthSpec("/some-route-without-correct-auth-to-show-failure", HttpMethod.POST, new Access.AnyRole(Role.ADMIN)),
        new RouteAuthSpec("/some-route-not-implemented-to-show-failure", HttpMethod.GET, new Access.Unauthenticated())
    );

    @Autowired
    RequestMappingHandlerMapping requestMapping;

    @Test
    void testsEveryRouteAndNoNonExistentRoutes() {
        Set<String> allRoutes = requestMapping.getHandlerMethods().keySet().stream()
            .filter((RequestMappingInfo info) ->
                info.getPatternsCondition() != null &&
                    !info.getPatternsCondition().getPatterns().contains("/error")
            ).flatMap((RequestMappingInfo info) -> {
                RequestMethod method = info.getMethodsCondition().getMethods().iterator().next();
                Set<String> routes = info.getPatternsCondition().getPatterns();
                return routes.stream().map((r) -> String.format("%s %s", method, r));
            }).collect(Collectors.toSet());

        Set<String> testedRoutes = routeAuthSpecs.stream()
            .map((s) -> String.format("%s %s", s.verb, s.route))
            .collect(Collectors.toSet());

        Set<String> untestedRoutes = allRoutes.stream()
            .filter((r) -> !testedRoutes.contains(r))
            .collect(Collectors.toSet());

        Set<String> nonexistentRoutes = testedRoutes.stream()
            .filter((r) -> !allRoutes.contains(r))
            .collect(Collectors.toSet());

        assertAll(
            () -> assertTrue(untestedRoutes.isEmpty(), String.format("The following routes are untested: %s", untestedRoutes)),
            () -> assertTrue(nonexistentRoutes.isEmpty(), String.format("Tests are defined for the following nonexistent routes: %s", nonexistentRoutes))
        );
    }

}