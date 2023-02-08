package com.esops.e2e


import com.esops.entity.*
import com.esops.service.UserService
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigInteger

@MicronautTest
class AccountInformationTest {
    @Inject
    lateinit var userService: UserService

    val commonUtil = CommonUtil()

    @BeforeEach
    fun `clear users`() {
        userService.clearUsers()
    }

    @Test
    fun `Should return error if user does not exist`(specification: RequestSpecification) {
        specification
            .given()
            .`when`()
            .contentType(ContentType.JSON)
            .pathParam("userName", "yash")
            .get("/user/{userName}/accountInformation")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.code)
            .body("error", hasItem("user does not exists"))
    }

    @Test
    fun `Should return account information if user exists`(specification: RequestSpecification) {
        userService.addUser(
            commonUtil.userRegistrationRequestBody(
                "John",
                "Convay",
                "john",
                "9236234576",
                "john@gmail.com"
            )
        )
        userService.addWalletMoney("john", commonUtil.addWalletMoneyRequestBody("10"))
        userService.addInventory("john", commonUtil.addInventoryRequestBody(EsopType.PERFORMANCE, "20"))
        userService.addInventory("john", commonUtil.addInventoryRequestBody(EsopType.NON_PERFORMANCE, "10"))
        val response = specification
            .given()
            .`when`()
            .contentType(ContentType.JSON)
            .pathParam("userName", "john")
            .get("/user/{userName}/accountInformation")
            .then()
            .statusCode(HttpStatus.OK.code)
            .extract()
            .`as`(FormattedUser::class.java)

        val expectedResponse = FormattedUser(
            "John",
            "Convay",
            "john",
            "john@gmail.com",
            "9236234576",
            Wallet(BigInteger.valueOf(10), BigInteger.ZERO),
            listOf(Inventory(EsopType.NON_PERFORMANCE), Inventory(EsopType.PERFORMANCE, BigInteger.valueOf(20))),
            listOf(
                UnvestedInventoryResponse("", BigInteger.valueOf(3)),
                UnvestedInventoryResponse("", BigInteger.valueOf(2)),
                UnvestedInventoryResponse("", BigInteger.valueOf(1)),
                UnvestedInventoryResponse("", BigInteger.valueOf(4))
            )
        )
        assertThat(response).usingRecursiveComparison().ignoringFields("unvestedInventoryList.time")
            .isEqualTo(expectedResponse)
    }
}