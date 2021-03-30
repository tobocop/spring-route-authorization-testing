package com.example.routeauthorizationtesting

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class WebSecurityConfig: WebSecurityConfigurerAdapter() {
    @Bean
    fun encoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.inMemoryAuthentication()
            .withUser("admin")
            .password(encoder().encode("pass"))
            .roles(Role.ADMIN.toString())
            .and()
            .withUser("basic")
            .password(encoder().encode("pass"))
            .roles(Role.BASIC.toString())
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.httpBasic()
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/user/*").hasAnyRole("ADMIN", "BASIC")
            .antMatchers(HttpMethod.POST, "/user").hasAnyRole("ADMIN")
            .antMatchers(HttpMethod.DELETE, "/user/*").hasAnyRole("ADMIN")
            .and()
            .csrf().disable()
            .formLogin().disable()
    }
}