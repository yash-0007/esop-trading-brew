package com.esops.controller

import com.esops.testUtility.CommonUtil
import com.esops.entity.EsopType
import com.esops.entity.Order
import com.esops.entity.OrderStatus
import com.esops.entity.OrderType
import com.esops.service.OrderService
import com.esops.service.UserService
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.common.mapper.TypeRef
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MicronautTest
class OrderControllerTest {
    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var orderService: OrderService

    private val commonUtil = CommonUtil()

    @BeforeEach
    fun `set up`() {
        userService.clearUsers()
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
                "orderId", CoreMatchers.equalTo("1"),
                "quantity", CoreMatchers.equalTo(10),
                "price", CoreMatchers.equalTo(50),
                "type", CoreMatchers.equalTo(OrderType.BUY.toString()),
                "status", CoreMatchers.equalTo(OrderStatus.PLACED.toString())
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
                "error", CoreMatchers.hasItem("insufficient wallet funds")
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
        specification.given().body(commonUtil.sellOrderRequest("10", "50", EsopType.PERFORMANCE)).contentType(
            ContentType.JSON
        )
            .`when`()
            .pathParam("userName", "john")
            .post("/user/{userName}/order")
            .then()
            .statusCode(200)
            .body(
                "orderId", CoreMatchers.equalTo("1"),
                "quantity", CoreMatchers.equalTo(10),
                "price", CoreMatchers.equalTo(50),
                "type", CoreMatchers.equalTo(OrderType.SELL.toString()),
                "status", CoreMatchers.equalTo(OrderStatus.PLACED.toString())
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
        specification.given().body(commonUtil.sellOrderRequest("10", "50", EsopType.PERFORMANCE)).contentType(
            ContentType.JSON
        )
            .`when`()
            .pathParam("userName", "u2")
            .post("/user/{userName}/order")
            .then()
            .statusCode(200)
            .body(
                "orderId", CoreMatchers.equalTo("2"),
                "quantity", CoreMatchers.equalTo(10),
                "price", CoreMatchers.equalTo(50),
                "type", CoreMatchers.equalTo(OrderType.SELL.toString()),
                "status", CoreMatchers.equalTo(OrderStatus.COMPLETE.toString())
            )
    }

    @Test
    fun `Should throw error if user does not exist`(specification: RequestSpecification) {
        specification
            .given()
            .`when`()
            .contentType(ContentType.JSON)
            .pathParam("userName", "yash")
            .get("/user/{userName}/order")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.code)
            .body("error", CoreMatchers.hasItem("user does not exists"))
    }

    @Test
    fun `Should return order history of user`(specification: RequestSpecification) {
        userService.addUser(commonUtil.userRegistrationRequestBody())
        userService.addWalletMoney("john", commonUtil.addWalletMoneyRequestBody("150"))
        orderService.placeOrder("john", commonUtil.buyOrderRequest("10", "5"))
        orderService.placeOrder("john", commonUtil.buyOrderRequest("10", "5"))
        orderService.placeOrder("john", commonUtil.buyOrderRequest("10", "5"))
        val orderList: List<Order> = specification
            .given()
            .`when`()
            .contentType(ContentType.JSON)
            .pathParam("userName", "john")
            .get("/user/{userName}/order")
            .then()
            .statusCode(HttpStatus.OK.code)
            .extract()
            .`as`(object : TypeRef<List<Order>>() {})

        Assertions.assertEquals(orderService.orderHistory("john"), orderList)
    }
}