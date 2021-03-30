package com.example.routeauthorizationtesting

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@SpringBootTest
@AutoConfigureMockMvc
internal class WebSecurityConfigTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var requestMapping: RequestMappingHandlerMapping

    @Autowired
    private lateinit var routeAuthSpecs: RouteAuthSpecs

    @Test
    fun `tests every route and no non-existent routes`() {
        val allRoutes = requestMapping.handlerMethods.keys
            .filter { !it.patternsCondition!!.patterns.contains("/error") }
            .flatMap { key ->
                val method = key.methodsCondition.methods.first()
                val routes = key.patternsCondition!!.patterns
                routes.map { route -> "$method $route" }
            }
        val testedRoutes = routeAuthSpecs.map { spec ->
            "${spec.verb} ${spec.route}"
        }
        val untestedRoutes: Set<String> = allRoutes.subtract(testedRoutes)
        Assertions.assertThat(untestedRoutes)
            .withFailMessage("The following routes are untested: %s", untestedRoutes)
            .isEmpty()

        val nonexistentRoutes = testedRoutes.minus("GET /index").subtract(allRoutes)
        Assertions.assertThat(nonexistentRoutes)
            .withFailMessage("Tests are defined for the following nonexistent routes: %s", nonexistentRoutes)
            .isEmpty()
    }

    @TestFactory
    fun `all roles have correct authorization`(): List<DynamicTest> =
        Role.values().flatMap { role ->
            routeAuthSpecs.map { spec ->
                DynamicTest.dynamicTest("$role [${spec.verb}] ${spec.route}") {
                    assertCorrectAuthz(role, spec)
                }
            }
        }

    private fun assertCorrectAuthz(role: Role, spec: RouteAuthSpec) {
        val result = mockMvc.perform(
            spec.request.with(
                SecurityMockMvcRequestPostProcessors.user("automation-user").roles(role.toString())
            )
        ).andReturn()

        Assertions.assertThat(result.response.status)
            .withFailMessage("Method ${spec.verb} does not exist for route ${spec.route}")
            .isNotEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value())

        when (spec.access) {
            is Access.AnyRole -> {
                if (spec.access.allowedForRole(role)) {
                    Assertions.assertThat(result.response.status)
                        .withFailMessage("Expected role $role to be PERMITTED to ${spec.verb} ${spec.route}")
                        .isNotEqualTo(HttpStatus.FORBIDDEN.value())
                } else {
                    Assertions.assertThat(result.response.status)
                        .withFailMessage("Expected role $role to be FORBIDDEN to ${spec.verb} ${spec.route}")
                        .isEqualTo(HttpStatus.FORBIDDEN.value())
                }
            }
            is Access.Unauthenticated -> {
                Assertions.assertThat(result.response.status)
                    .withFailMessage("Expected role $role to be PERMITTED to ${spec.verb} ${spec.route}")
                    .isNotEqualTo(HttpStatus.FORBIDDEN.value())
            }
        }
    }
}