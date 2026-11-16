package com.hms.hospital.controller;

import com.hms.hospital.security.JwtAuthenticationFilter;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @WebMvcTest for a single controller, with the security filter chain switched off.
 *
 * These slice tests exist to check controller behaviour — routing, validation, status codes,
 * error shapes. Authentication/authorization is a separate concern with its own dedicated tests
 * (see SecurityIntegrationTest), which run the real filter chain against the real policy. Keeping
 * the two apart means a change to the access policy doesn't ripple through 25 controller tests,
 * and a controller test can't accidentally pass or fail because of a missing token.
 *
 * JwtAuthenticationFilter is excluded because @WebMvcTest picks up every Filter bean, and that
 * one needs services the slice deliberately doesn't load.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@WebMvcTest(excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
public @interface ControllerTest {

    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] value() default {};
}
