package com.esops.controller

import com.esops.entity.FormattedUser
import com.esops.model.*
import com.esops.service.UserService
import io.micronaut.http.*
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import javax.validation.Valid

@Validated
@Controller
class UserController(
    private var userService: UserService
) {

    @Post(uri = "/user/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun register(@Body @Valid userRegistrationRequestBody: UserRegistrationRequestBody): HttpResponse<UserRegistrationResponseBody> =
        HttpResponse.created(this.userService.addUser(userRegistrationRequestBody))

    @Get(uri = "/user/{userName}/accountInformation")
    @Produces(MediaType.APPLICATION_JSON)
    fun accountInformation(userName: String): HttpResponse<FormattedUser> =
        HttpResponse.ok(this.userService.accountInformation(userName))
}
