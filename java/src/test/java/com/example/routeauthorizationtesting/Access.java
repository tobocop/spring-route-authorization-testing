package com.example.routeauthorizationtesting;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

class Access {
    static class Unauthenticated extends Access {
    }

    static class AnyRole extends Access {
        List<Role> roles;

        AnyRole(Role... roles) {
            this.roles = Arrays.stream(roles).collect(toList());
        }

        Boolean allowedForRole(Role role) {
            return this.roles.contains(role);
        }
    }
}
