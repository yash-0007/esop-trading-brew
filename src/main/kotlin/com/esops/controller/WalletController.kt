package com.esops.controller

import com.esops.model.AddWalletMoneyRequestBody
import com.esops.model.AddWalletMoneyResponseBody
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
class WalletController(
    private var userService: UserService
) {
    @Post(uri = "/user/{userName}/wallet")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun walletMoney(
        @Body @Valid addWalletMoneyRequestBody: AddWalletMoneyRequestBody,
        userName: String
    ): HttpResponse<AddWalletMoneyResponseBody> =
        HttpResponse.ok(this.userService.addWalletMoney(userName, addWalletMoneyRequestBody))

}