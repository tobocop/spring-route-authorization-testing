package com.example.routeauthorizationtesting

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@SpringBootTest
@AutoConfigureMockMvc
internal class WebSecurityConfigTest {

    val routeAuthSpecs: Set<RouteAuthSpec> = setOf(
        RouteAuthSpec("/user/{id}", HttpMethod.GET, Access.Unauthenticated),
        RouteAuthSpec("/user/{id}", HttpMethod.PUT, Access.AnyRole(Role.ADMIN, Role.BASIC)),
        RouteAuthSpec("/user/{id}", HttpMethod.PATCH, Access.AnyRole(Role.ADMIN, Role.BASIC)),
        RouteAuthSpec("/user", HttpMethod.POST, Access.AnyRole(Role.ADMIN)),
        RouteAuthSpec("/user/{id}", HttpMethod.DELETE, Access.AnyRole(Role.ADMIN)),
        RouteAuthSpec("/some-route-without-correct-auth-to-show-failure", HttpMethod.POST, Access.AnyRole(Role.ADMIN)),
        RouteAuthSpec("/some-route-not-implemented-to-show-failure", HttpMethod.GET, Access.Unauthenticated),
    )

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var requestMapping: RequestMappingHandlerMapping

    @Test
    fun `tests every route and no non-existent routes`() {
        val allRoutes = requestMapping.handlerMethods.keys
            .filter { it.patternsCondition != null }
            .filterNot { it.patternsCondition!!.patterns.contains("/error") }
            .flatMap { requestMappingInfo ->
                val method = requestMappingInfo.methodsCondition.methods.first()
                val routes = requestMappingInfo.patternsCondition!!.patterns
                routes.map { route -> "$method $route" }
            }
        val testedRoutes = routeAuthSpecs.map { spec ->
            "${spec.verb} ${spec.route}"
        }

        val untestedRoutes: Set<String> = allRoutes.subtract(testedRoutes)
        val nonexistentRoutes = testedRoutes.minus("GET /index").subtract(allRoutes)

        assertAll({
            assertThat(untestedRoutes)
                .withFailMessage("The following routes are untested: %s", untestedRoutes)
                .isEmpty()
        }, {
            assertThat(nonexistentRoutes)
                .withFailMessage("Tests are defined for the following nonexistent routes: %s", nonexistentRoutes)
                .isEmpty()
        })
    }


    @TestFactory
    fun `all routes have correct authorization when authenticated`(): List<DynamicTest> =
        Role.values().flatMap { role ->
            routeAuthSpecs.map { spec ->
                dynamicTest("$role [${spec.verb}] ${spec.route}") {
                    assertCorrectAuthz(role, spec)
                }
            }
        }

    @TestFactory
    fun `all routes have correct authorization when unauthenticated`(): List<DynamicTest> =
        routeAuthSpecs.map { spec ->
            dynamicTest("Unauthenticated [${spec.verb}] ${spec.route}") {
                assertCorrectUnauthenticated(spec)
            }
        }

    private fun assertCorrectAuthz(role: Role, spec: RouteAuthSpec) {
        val result = mockMvc.perform(
            spec.request.with(
                SecurityMockMvcRequestPostProcessors
                    .user("automation-user")
                    .roles(role.toString())
            )
        ).andReturn()

        when (spec.access) {
            is Access.AnyRole -> {
                if (spec.access.allowedForRole(role)) {
                    assertThat(result.response.status)
                        .withFailMessage("Expected role $role to be PERMITTED to ${spec.verb} ${spec.route}")
                        .isNotEqualTo(HttpStatus.FORBIDDEN.value())
                } else {
                    assertThat(result.response.status)
                        .withFailMessage("Expected role $role to be FORBIDDEN to ${spec.verb} ${spec.route}")
                        .isEqualTo(HttpStatus.FORBIDDEN.value())
                }
            }
            is Access.Unauthenticated -> {
                assertThat(result.response.status)
                    .withFailMessage("Expected role $role to be PERMITTED to ${spec.verb} ${spec.route}")
                    .isNotEqualTo(HttpStatus.FORBIDDEN.value())
            }
        }
    }

    private fun assertCorrectUnauthenticated(spec: RouteAuthSpec) {
        val result = mockMvc.perform(spec.request).andReturn()

        assertThat(result.response.status)
            .withFailMessage("Method ${spec.verb} does not exist for route ${spec.route}")
            .isNotEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value())

        when (spec.access) {
            is Access.Unauthenticated -> {
                assertThat(result.response.status)
                    .withFailMessage("Expected ${spec.verb} ${spec.route} not to require authentication")
                    .isNotEqualTo(HttpStatus.UNAUTHORIZED.value())
            }
            else -> {
                assertThat(result.response.status)
                    .withFailMessage("Expected ${spec.verb} ${spec.route} to require authentication.")
                    .isEqualTo(HttpStatus.UNAUTHORIZED.value())
            }
        }
    }
}