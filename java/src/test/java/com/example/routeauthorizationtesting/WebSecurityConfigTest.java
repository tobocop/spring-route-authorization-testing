package com.example.routeauthorizationtesting;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest
//@AutoConfigureMockMvc
class WebSecurityConfigTest {
    @Autowired
    RequestMappingHandlerMapping requestMapping;

    @Autowired
    Set<RouteAuthSpec> routeAuthSpecs;

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

        Assertions.assertThat(untestedRoutes)
            .withFailMessage("The following routes are untested: %s", untestedRoutes)
            .isEmpty();

        Set<String> nonExistentRoutes = testedRoutes.stream()
            .filter((r) -> !allRoutes.contains(r))
            .collect(Collectors.toSet());

        Assertions.assertThat(nonExistentRoutes)
            .withFailMessage("Tests are defined for the following nonexistent routes: %s", nonExistentRoutes)
            .isEmpty();
    }

}