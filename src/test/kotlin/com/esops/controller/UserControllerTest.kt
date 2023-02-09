package com.esops.controller

import com.esops.configuration.InventoryLimitConfiguration
import com.esops.configuration.WalletLimitConfiguration
import com.esops.testUtility.CommonUtil
import com.esops.entity.*
import com.esops.service.UserService
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import org.assertj.core.api.Assertions
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigInteger

@MicronautTest
class UserControllerTest {
    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var walletLimitConfiguration: WalletLimitConfiguration

    @Inject
    lateinit var inventoryLimitConfiguration: InventoryLimitConfiguration

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
                CoreMatchers.hasItems(
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
                "error", CoreMatchers.hasItems("Required Body [userRegistrationRequestBody] not specified")
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
                "error", CoreMatchers.hasItems(
                    "userName must contain one alphanumeric character",
                    "userName cannot contain special characters \$&+,/:;=?@/s\"<>#%}|\\^~[]`",
                    "userName should have 2 to 100 characters"
                )
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
                CoreMatchers.hasItems(
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
                "firstName", CoreMatchers.equalTo("John"),
                "lastName", CoreMatchers.equalTo("Doe"),
                "phoneNumber", CoreMatchers.equalTo("9524125143"),
                "email", CoreMatchers.equalTo("john.doe@example.com"),
                "userName", CoreMatchers.equalTo("john_doe1"),
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
                "error",
                CoreMatchers.hasItems("userName already exists", "email already exists", "phoneNumber already exists")
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
                "error", CoreMatchers.hasItems("userName should have 2 to 100 characters")
            )
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
            .body("error", CoreMatchers.hasItem("user does not exists"))
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
            listOf(
                Inventory(EsopType.NON_PERFORMANCE, BigInteger.valueOf(10)),
                Inventory(EsopType.PERFORMANCE, BigInteger.valueOf(20))
            ),
            listOf(
                UnvestedInventoryResponse("", BigInteger.valueOf(3)),
                UnvestedInventoryResponse("", BigInteger.valueOf(2)),
                UnvestedInventoryResponse("", BigInteger.valueOf(1)),
                UnvestedInventoryResponse("", BigInteger.valueOf(4))
            )
        )
        Assertions.assertThat(response).usingRecursiveComparison().ignoringFields("unvestedInventoryList")
            .isEqualTo(expectedResponse)
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
            .body("message", CoreMatchers.equalTo("50 amount added to your account"))
    }

    @Test
    fun `Exceed wallet limit of user`(specification: RequestSpecification) {
        userService.addUser(commonUtil.userRegistrationRequestBody())
        userService.addWalletMoney("john", commonUtil.addWalletMoneyRequestBody("5000"))
        specification
            .given()
            .body(
                commonUtil.addWalletMoneyRequestBody(
                    BigInteger(walletLimitConfiguration.max!!).subtract(
                        BigInteger.valueOf(
                            5000 - 1
                        )
                    ).toString()
                )
            )
            .contentType(ContentType.JSON)
            .`when`()
            .pathParam("userName", "john")
            .post("/user/{userName}/wallet")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .body("error", CoreMatchers.hasItem("Total Wallet limit (${walletLimitConfiguration.max}) exceeded"))
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
            .body("error", CoreMatchers.hasItem("Total Wallet limit (${walletLimitConfiguration.max}) exceeded"))
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
            .body("message", CoreMatchers.equalTo("50 ESOPs added to your account"))
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
            .body("message", CoreMatchers.equalTo("50 PERFORMANCE ESOPs added to your account"))
    }

    @Test
    fun `Exceed inventory limit of user`(specification: RequestSpecification) {
        userService.addUser(commonUtil.userRegistrationRequestBody())
        userService.addInventory("john", commonUtil.addInventoryRequestBody(EsopType.PERFORMANCE, "5000"))
        specification
            .given()
            .body(
                commonUtil.addInventoryRequestBody(
                    EsopType.PERFORMANCE,
                    BigInteger(inventoryLimitConfiguration.max!!).subtract(BigInteger.valueOf(5000 - 1)).toString()
                )
            )
            .contentType(ContentType.JSON)
            .`when`()
            .pathParam("userName", "john")
            .post("/user/{userName}/inventory")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .body("error", CoreMatchers.hasItem("Inventory limit (${inventoryLimitConfiguration.max}) exceeded"))
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
            .body("error", CoreMatchers.hasItem("Inventory limit (${inventoryLimitConfiguration.max}) exceeded"))
    }

}