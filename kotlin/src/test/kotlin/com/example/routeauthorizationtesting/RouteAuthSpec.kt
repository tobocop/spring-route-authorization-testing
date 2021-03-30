package com.example.routeauthorizationtesting

import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.*
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*


data class RouteAuthSpec(
    val route: String,
    val verb: HttpMethod,
    val access: Access
) {
    val request: MockHttpServletRequestBuilder
        get() {
            val sanitizedRoute = route.replace(Regex("\\{\\w+\\}"), "0")
            return when (verb) {
                GET -> get(sanitizedRoute)
                POST -> post(sanitizedRoute).with(csrf())
                PUT -> put(sanitizedRoute).with(csrf())
                DELETE -> delete(sanitizedRoute).with(csrf())
                PATCH -> patch(sanitizedRoute).with(csrf())
                else -> TODO("Not Implemented for method $verb")
            }
        }

    override fun toString(): String {
        return "submits a $verb to $route"
    }
}

sealed class Access {
    object Unauthenticated : Access()
    class AnyRole(private vararg val roles: Role) : Access() {
        fun allowedForRole(role: Role): Boolean {
            return roles.contains(role)
        }
    }
}
