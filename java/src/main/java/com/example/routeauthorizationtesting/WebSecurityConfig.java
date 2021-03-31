package com.example.routeauthorizationtesting;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Bean
    PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
            .withUser("admin")
            .password(encoder().encode("pass"))
            .roles(Role.ADMIN.toString())
            .and()
            .withUser("basic")
            .password(encoder().encode("pass"))
            .roles(Role.BASIC.toString());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic()
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/user/*").hasAnyRole("ADMIN", "BASIC")
            .antMatchers(HttpMethod.POST, "/user").hasAnyRole("ADMIN")
            .antMatchers(HttpMethod.DELETE, "/user/*").hasAnyRole("ADMIN")
            .and()
            .csrf().disable()
            .formLogin().disable();
    }

}
