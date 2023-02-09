package com.esops.controller

import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.hamcrest.CoreMatchers.hasItem
import org.junit.jupiter.api.Test

@MicronautTest
class GlobalExceptionControllerTest {
    @Test
    fun `Invalid endpoint`(spec: RequestSpecification) {
        spec.given().body("")
                .contentType(ContentType.JSON)
                .`when`()
                .post("/abc")
                .then().statusCode(HttpStatus.NOT_FOUND.code)
                .body("error", hasItem("not a valid endpoint"))
    }

    @Test
    fun `Invalid JSON`(spec: RequestSpecification) {
        spec.given().body("{")
                .contentType(ContentType.JSON)
                .`when`()
                .post("/user/register")
                .then().statusCode(HttpStatus.BAD_REQUEST.code)
                .body("error", hasItem("could not parse json"))
    }
}