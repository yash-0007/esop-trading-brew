package com.esops.e2e

import com.esops.configuration.WalletLimitConfiguration
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
class WalletTest {
    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var walletLimitConfiguration: WalletLimitConfiguration

    private val commonUtil = CommonUtil()

    @BeforeEach
    fun `clear users`() {
        userService.clearUsers()
    }

    @Test
    fun `Add to wallet`(specification: RequestSpecification) {
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
            .body(commonUtil.addWalletMoneyRequestBody("50"))
            .contentType(ContentType.JSON)
            .`when`()
            .pathParam("userName", "john")
            .post("/user/{userName}/wallet")
            .then()
            .statusCode(200)
            .body("message", equalTo("50 amount added to your account"))
    }

    @Test
    fun `Exceed wallet limit of user`(specification: RequestSpecification) {
        userService.addUser(commonUtil.userRegistrationRequestBody())
        userService.addWalletMoney("john", commonUtil.addWalletMoneyRequestBody("5000"))
        specification
            .given()
            .body(commonUtil.addWalletMoneyRequestBody(BigInteger(walletLimitConfiguration.max!!).subtract(BigInteger.valueOf(5000 - 1)).toString()))
            .contentType(ContentType.JSON)
            .`when`()
            .pathParam("userName", "john")
            .post("/user/{userName}/wallet")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .body("error", hasItem("Total Wallet limit (${walletLimitConfiguration.max}) exceeded"))
    }

    @Test
    fun `Overflow Wallet limit of the system`(specification: RequestSpecification) {
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
            .body(commonUtil.addWalletMoneyRequestBody(walletLimitConfiguration.max!! + 1))
            .contentType(ContentType.JSON)
            .`when`()
            .pathParam("userName", "john")
            .post("/user/{userName}/wallet")
            .then()
            .statusCode(400)
            .body("error", hasItem("value not in wallet limits of the system"))
    }
}
