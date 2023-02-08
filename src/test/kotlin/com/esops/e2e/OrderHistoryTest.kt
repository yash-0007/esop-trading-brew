package com.esops.e2e

import com.esops.entity.Order
import com.esops.service.OrderService
import com.esops.service.UserService
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.common.mapper.TypeRef
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import org.hamcrest.CoreMatchers.hasItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MicronautTest
class OrderHistoryTest {
    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var orderService: OrderService

    private val commonUtil = CommonUtil()

    @BeforeEach
    fun setUp() {
        userService.clearUsers()
        orderService.clearOrders()
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
            .body("error", hasItem("user does not exists"))
    }

    @Test
    fun `Should return order history of user`(specification: RequestSpecification) {
        userService.addUser(commonUtil.userRegistrationRequestBody())
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

        assertEquals(orderService.orderHistory("john"), orderList)
    }
}