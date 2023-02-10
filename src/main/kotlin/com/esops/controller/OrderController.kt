package com.esops.controller

import com.esops.entity.Order
import com.esops.model.AddOrderRequestBody
import com.esops.service.OrderService
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import javax.validation.Valid

@Validated
@Controller
class OrderController(
    private val orderService: OrderService
) {

    @Post(uri = "/user/{userName}/order")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun order(@Body @Valid addOrderRequestBody: AddOrderRequestBody, userName: String): HttpResponse<Order> =
        HttpResponse.ok(this.orderService.placeOrder(userName, addOrderRequestBody))

    @Get(uri = "/user/{userName}/order")
    @Produces(MediaType.APPLICATION_JSON)
    fun orderHistory(userName: String): HttpResponse<List<Order>> =
        HttpResponse.ok(this.orderService.orderHistory(userName))
}