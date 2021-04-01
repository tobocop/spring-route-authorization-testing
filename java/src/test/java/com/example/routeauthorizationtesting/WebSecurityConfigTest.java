package com.example.routeauthorizationtesting;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@SpringBootTest
@AutoConfigureMockMvc
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
    MockMvc mockMvc;

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

    @TestFactory
    List<DynamicTest> allRoutesHaveCorrectAuthorizationWhenUnauthenticated() {
        return routeAuthSpecs.stream().map((spec) -> dynamicTest(
            String.format("Unauthenticated [%s] %s", spec.verb, spec.route), () -> {
                assertCorrectUnauthenticated(spec);
            }
        )).collect(Collectors.toList());
    }

    @TestFactory
    List<DynamicTest> allRoutesHaveCorrectAuthorizationWhenAuthenticated() {
        return Arrays.stream(Role.values()).flatMap((role) ->
            routeAuthSpecs.stream().map((spec) -> dynamicTest(
                String.format("%s [%s] %s", role, spec.verb, spec.route), () -> {
                    assertCorrectAuthz(role, spec);
                }
            ))
        ).collect(Collectors.toList());
    }

    void assertCorrectUnauthenticated(RouteAuthSpec spec) throws Exception {
        MvcResult result = mockMvc.perform(spec.getRequest()).andReturn();

        assertNotEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), result.getResponse().getStatus(),
            String.format("Method %s does not exist for route %s", spec.verb, spec.route));

        if (spec.access instanceof Access.Unauthenticated) {
            assertNotEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus(),
                String.format("Expected %s %s not to require authentication", spec.verb, spec.route));
        } else {
            assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus(),
                String.format("Expected %s %s to require authentication.", spec.verb, spec.route));
        }
    }


    void assertCorrectAuthz(Role role, RouteAuthSpec spec) throws Exception {
        MvcResult result = mockMvc.perform(
            spec.getRequest().with(
                SecurityMockMvcRequestPostProcessors
                    .user("automation-user")
                    .roles(role.toString())
            )
        ).andReturn();

        if (spec.access instanceof Access.AnyRole) {
            if (((Access.AnyRole) spec.access).allowedForRole(role)) {
                assertNotEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus(),
                    String.format("Expected role %s to be PERMITTED to %s %s", role, spec.verb, spec.route));
            } else {
                assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus(),
                    String.format("Expected role %s to be FORBIDDEN to %s %s", role, spec.verb, spec.route));
            }
        } else if (spec.access instanceof Access.Unauthenticated) {
            assertNotEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus(),
                String.format("Expected role %s to be PERMITTED to %s %s", role, spec.verb, spec.route));
        } else {
            throw new Error("Unknown class passed for spec.access");
        }
    }
}
