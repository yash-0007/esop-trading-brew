package com.esops.e2e

import com.esops.configuration.InventoryLimitConfiguration
import com.esops.configuration.WalletLimitConfiguration
import com.esops.entity.EsopType
import com.esops.service.OrderService
import com.esops.service.UserService
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigInteger

@MicronautTest
class InventoryTest {
    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var inventoryLimitConfiguration: InventoryLimitConfiguration

    private val commonUtil = CommonUtil()

    @BeforeEach
    fun `clear user`() {
        userService.clearUsers()
    }

    @Test
    fun `Add to NON_PERFORMANCE inventory`(specification: RequestSpecification) {
        userService.addUser(
            commonUtil.userRegistrationRequestBody(
                "John",
                "Doe",
                "john",
                "9524125143",
                "e2e2@gmail.com"
            )
        )
        specification
            .given()
            .body(commonUtil.addInventoryRequestBody(EsopType.NON_PERFORMANCE, "50"))
            .contentType(ContentType.JSON)
            .`when`()
            .pathParam("userName", "john")
            .post("/user/{userName}/inventory")
            .then()
            .statusCode(200)
            .body("message", equalTo("50 ESOPs added to your account"))
    }

    @Test
    fun `Add to PERFORMANCE inventory`(specification: RequestSpecification) {
        userService.addUser(
            commonUtil.userRegistrationRequestBody(
                "John",
                "Doe",
                "john",
                "9524125143",
                "e2e2@gmail.com"
            )
        )
        specification
            .given()
            .body(commonUtil.addInventoryRequestBody(EsopType.PERFORMANCE, "50"))
            .contentType(ContentType.JSON)
            .`when`()
            .pathParam("userName", "john")
            .post("/user/{userName}/inventory")
            .then()
            .statusCode(200)
            .body("message", equalTo("50 PERFORMANCE ESOPs added to your account"))
    }

    @Test
    fun `Exceed wallet limit of user`(specification: RequestSpecification) {
        userService.addUser(commonUtil.userRegistrationRequestBody())
        userService.addInventory("john", commonUtil.addInventoryRequestBody(EsopType.PERFORMANCE,"5000"))
        specification
            .given()
            .body(commonUtil.addInventoryRequestBody(EsopType.PERFORMANCE, BigInteger(inventoryLimitConfiguration.max!!).subtract(BigInteger.valueOf(5000 - 1)).toString()))
            .contentType(ContentType.JSON)
            .`when`()
            .pathParam("userName", "john")
            .post("/user/{userName}/inventory")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .body("error", hasItem("Inventory limit (${inventoryLimitConfiguration.max}) exceeded"))
    }

    @Test
    fun `Overflow inventory limit of the system`(specification: RequestSpecification) {
        userService.addUser(
            commonUtil.userRegistrationRequestBody(
                "John",
                "Doe",
                "john",
                "9524125143",
                "e2e2@gmail.com"
            )
        )
        specification
            .given()
            .body(commonUtil.addInventoryRequestBody(EsopType.PERFORMANCE, "1000000000000000000001"))
            .contentType(ContentType.JSON)
            .`when`()
            .pathParam("userName", "john")
            .post("/user/{userName}/inventory")
            .then()
            .statusCode(400)
            .body("error", hasItem("value not in inventory limits of the system"))
    }
}