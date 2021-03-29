package com.example.routeauthorizationtesting

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@SpringBootTest
class RouteSpecsIncludeAllApplicationRoutesTest {
    @Autowired
    lateinit var requestMapping: RequestMappingHandlerMapping

    @Autowired
    lateinit var actuatorMapping: PathMappedEndpoints

    @Autowired
    lateinit var authorizationSpecs: AuthorizationSpecs

    @Test
    fun `tests every route and no non-existent routes`() {
        val allRoutes = requestMapping.handlerMethods.keys
            .filter { !it.patternsCondition!!.patterns.contains("/error") }
            .flatMap { key ->
                val method = key.methodsCondition.methods.first()
                val routes = key.patternsCondition!!.patterns
                routes.map { route -> "$method $route" }
            }.toSet().plus(
                actuatorMapping.allPaths.map { "GET $it" }
            )
        val testedRoutes = authorizationSpecs.map { spec ->
            "${spec.verb} ${spec.route}"
        }
        val untestedRoutes: Set<String> = allRoutes.subtract(testedRoutes)
        assertThat(untestedRoutes)
            .withFailMessage("The following routes are untested: %s", untestedRoutes)
            .isEmpty()

        val nonexistentRoutes = testedRoutes.minus("GET /index").subtract(allRoutes)
        assertThat(nonexistentRoutes)
            .withFailMessage("Tests are defined for the following nonexistent routes: %s", nonexistentRoutes)
            .isEmpty()
    }

//    @Test
//    fun `tests every role`() {
//        val testedRoles = RouteAuthorizationTest::class.members
//            .map { it.findAnnotation<WithMockUser>() }
//            .filterNotNull()
//            .flatMap { it.roles.asIterable() }
//            .toSet()
//
//        val allRoles = Role.values().map { it.toString() }.toSet()
//
//        val untestedRoles = allRoles.subtract(testedRoles)
//        assertThat(untestedRoles)
//            .withFailMessage("The following roles are untested: %s", untestedRoles)
//            .isEmpty()
//    }
}
