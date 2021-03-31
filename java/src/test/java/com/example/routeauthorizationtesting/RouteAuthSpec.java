package com.example.routeauthorizationtesting;

import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class RouteAuthSpec {
    String route;
    HttpMethod verb;
    Access access;

    RouteAuthSpec(String route, HttpMethod verb, Access access) {
        this.route = route;
        this.verb = verb;
        this.access = access;
    }

    MockHttpServletRequestBuilder getRequest() {
        String sanitizedRoute = route.replaceAll("\\{\\w+}", "0");
        switch (this.verb) {
            case GET:
                return get(sanitizedRoute);
            case POST:
                return post(sanitizedRoute).with(csrf());
            case PUT:
                return put(sanitizedRoute).with(csrf());
            case PATCH:
                return patch(sanitizedRoute).with(csrf());
            case DELETE:
                return delete(sanitizedRoute).with(csrf());
            default:
                throw new Error(String.format("Method %s not implemented", this.verb.toString()));
        }
    }
}

