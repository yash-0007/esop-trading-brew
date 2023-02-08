package com.esops.e2e

import com.esops.entity.EsopType
import com.esops.entity.OrderStatus
import com.esops.entity.OrderType
import com.esops.service.OrderService
import com.esops.service.UserService
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MicronautTest
class CreateOrderTest {
    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var orderService: OrderService

    private val commonUtil =  CommonUtil()

    @BeforeEach
    fun `clear user`() {
        userService.clearUsers()
    }

    @BeforeEach
    fun `clear order`() {
        orderService.clearOrders()
    }

    @Test
    fun `Place buy order`(specification: RequestSpecification) {
        userService.addUser(
            commonUtil.userRegistrationRequestBody(
                "John",
                "Doe",
                "john",
                "9524125143",
                "e2e2@gmail.com"
            )
        )
        userService.addWalletMoney("john", commonUtil.addWalletMoneyRequestBody("500"))
        specification.given().body(commonUtil.buyOrderRequest("10", "50")).contentType(ContentType.JSON)
            .`when`()
            .pathParam("userName", "john")
            .post("/user/{userName}/order")
            .then()
            .statusCode(200)
            .body(
                "orderId", equalTo("1"),
                "quantity", equalTo(10),
                "price", equalTo(50),
                "type", equalTo(OrderType.BUY.toString()),
                "status", equalTo(OrderStatus.PLACED.toString())
            )
    }

    @Test
    fun `Do not place buy order in case insufficient funds`(specification: RequestSpecification) {
        userService.addUser(
            commonUtil.userRegistrationRequestBody(
                "John",
                "Doe",
                "john",
                "9524125143",
                "e2e2@gmail.com"
            )
        )
        specification.given().body(commonUtil.buyOrderRequest("10", "50")).contentType(ContentType.JSON)
            .`when`()
            .pathParam("userName", "john")
            .post("/user/{userName}/order")
            .then()
            .statusCode(400)
            .body(
                "error", hasItem("insufficient wallet funds")
            )
    }

    @Test
    fun `Place sell order`(specification: RequestSpecification) {
        userService.addUser(
            commonUtil.userRegistrationRequestBody(
                "John",
                "Doe",
                "john",
                "9524125143",
                "e2e2@gmail.com"
            )
        )
        userService.addInventory("john", commonUtil.addInventoryRequestBody(EsopType.PERFORMANCE, "10"))
        specification.given().body(commonUtil.sellOrderRequest("10", "50", EsopType.PERFORMANCE)).contentType(ContentType.JSON)
            .`when`()
            .pathParam("userName", "john")
            .post("/user/{userName}/order")
            .then()
            .statusCode(200)
            .body(
                "orderId", equalTo("1"),
                "quantity", equalTo(10),
                "price", equalTo(50),
                "type", equalTo(OrderType.SELL.toString()),
                "status", equalTo(OrderStatus.PLACED.toString())
            )
    }
    @Test
    fun `Place and match buy and sell order`(specification: RequestSpecification) {
        userService.addUser(
            commonUtil.userRegistrationRequestBody(
                "John",
                "Doe",
                "u1",
                "9524125142",
                "e2e1@gmail.com"
            )
        )
        userService.addUser(
            commonUtil.userRegistrationRequestBody(
                "John",
                "Doe",
                "u2",
                "9524125143",
                "e2e2@gmail.com"
            )
        )
        userService.addWalletMoney("u1", commonUtil.addWalletMoneyRequestBody("500"))
        userService.addInventory("u2", commonUtil.addInventoryRequestBody(EsopType.PERFORMANCE, "10"))
        orderService.placeOrder("u1", commonUtil.buyOrderRequest("10", "50"))
        specification.given().body(commonUtil.sellOrderRequest("10", "50", EsopType.PERFORMANCE)).contentType(ContentType.JSON)
            .`when`()
            .pathParam("userName", "u2")
            .post("/user/{userName}/order")
            .then()
            .statusCode(200)
            .body(
                "orderId", equalTo("2"),
                "quantity", equalTo(10),
                "price", equalTo(50),
                "type", equalTo(OrderType.SELL.toString()),
                "status", equalTo(OrderStatus.COMPLETE.toString())
            )
    }
}