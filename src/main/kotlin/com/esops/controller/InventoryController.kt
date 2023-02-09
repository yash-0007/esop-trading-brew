package com.esops.controller

import com.esops.model.AddInventoryRequestBody
import com.esops.model.AddInventoryResponseBody
import com.esops.service.UserService
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.validation.Validated
import javax.validation.Valid

@Validated
@Controller
class InventoryController(
    private var userService: UserService
) {

    @Post(uri = "/user/{userName}/inventory")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun inventory(
        @Body @Valid addInventoryRequestBody: AddInventoryRequestBody,
        userName: String
    ): HttpResponse<AddInventoryResponseBody> =
        HttpResponse.ok(this.userService.addInventory(userName, addInventoryRequestBody))

}