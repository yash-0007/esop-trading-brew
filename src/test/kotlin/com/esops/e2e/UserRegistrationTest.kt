package com.esops.e2e

import com.esops.service.UserService
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MicronautTest
class UserRegistrationTest {
    @Inject
    lateinit var userService: UserService

    private val commonUtil = CommonUtil()

    @BeforeEach
    fun `clear all user`() {
        userService.clearUsers()
    }

    @Test
    fun `Should throw error if there are missing fields`(spec: RequestSpecification) {
        spec.given().body("{}")
            .contentType(ContentType.JSON)
            .`when`().post("/user/register")
            .then()
            .statusCode(400)
            .body(
                "error",
                hasItems(
                    "email field is blank",
                    "phoneNumber field is blank",
                    "firstName field is blank"
                )
            )
    }

    @Test
    fun `Should throw error in case of empty JSON request body`(spec: RequestSpecification) {
        spec.given().body("")
            .contentType(ContentType.JSON)
            .`when`()
            .post("/user/register")
            .then().statusCode(400)
            .body(
                "error", hasItems("Required Body [userRegistrationRequestBody] not specified")
            )
    }

    @Test
    fun `Should throw error if userName field contains special characters`(spec: RequestSpecification) {
        spec.given().body(
            commonUtil.userRegistrationRequestBody(
                "John",
                "Doe",
                "/",
                "9524125143",
                "e2e2@gmail.com"
            )
        )
            .contentType(ContentType.JSON)
            .`when`()
            .post("/user/register")
            .then().statusCode(400)
            .body(
                "error", hasItems("userName cannot contain special characters \$&+,/:;=?@/s\"<>#%{}|\\^~[]`")
            )
    }

    @Test
    fun `Should throw error if userName field does not contain least one alphanumeric character`(spec: RequestSpecification) {
        spec.given().body(
            commonUtil.userRegistrationRequestBody(
                "John",
                "Doe",
                "_",
                "9524125143",
                "e2e2@gmail.com"
            )
        )
            .contentType(ContentType.JSON)
            .`when`()
            .post("/user/register")
            .then().statusCode(400)
            .body(
                "error",
                hasItems(
                    "userName must contain one alphanumeric character",
                    "userName should have 2 to 100 characters"
                )
            )
    }

    @Test
    fun `Should create new user`(spec: RequestSpecification) {
        spec.given().body(
            """
            {"firstName": "John",
                "lastName": "Doe",
                "phoneNumber": "9524125143",
                "email": "john.doe@example.com",
                "userName": "john_doe1"}
        """.trimIndent()

        )
            .contentType(ContentType.JSON)
            .`when`()
            .post("/user/register")
            .then().statusCode(201)
            .body(
                "firstName", equalTo("John"),
                "lastName", equalTo("Doe"),
                "phoneNumber", equalTo("9524125143"),
                "email", equalTo("john.doe@example.com"),
                "userName", equalTo("john_doe1"),
            )
    }

    @Test
    fun `Should not create new user with same userName, phoneNumber and email`(spec: RequestSpecification) {
        userService.addUser(
            commonUtil.userRegistrationRequestBody(
                "John",
                "Doe",
                "john",
                "9524125143",
                "e2e2@gmail.com"
            )
        )
        spec.given().body(
            commonUtil.userRegistrationRequestBody(
                "John",
                "Doe",
                "john",
                "9524125143",
                "e2e2@gmail.com"
            )
        )
            .contentType(ContentType.JSON)
            .`when`()
            .post("/user/register")
            .then().statusCode(400)
            .body(
                "error", hasItems("userName already exists", "email already exists", "phoneNumber already exists")
            )
    }

    @Test
    fun `Should throw error if userName has more than 100 characters`(spec: RequestSpecification) {
        spec.given().body(
            commonUtil.userRegistrationRequestBody(
                "John",
                "Doe",
                "johnjohnjohnjohnjohnjohnjohnjohnjohnjohnjohnjohnjohnjohnjohnjohnjohnjohnjohnjohnjohnjohnjohnjohnjohn2",
                "9524125143",
                "e2e2@gmail.com"
            )
        )
            .contentType(ContentType.JSON)
            .`when`()
            .post("/user/register")
            .then().statusCode(HttpStatus.BAD_REQUEST.code)
            .body(
                "error", hasItems("userName should have 2 to 100 characters")
            )
    }

}