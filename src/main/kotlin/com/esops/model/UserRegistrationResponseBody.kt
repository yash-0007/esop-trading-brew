package com.esops.model

data class UserRegistrationResponseBody(
    val userName: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String
) {
    constructor(requestBody: UserRegistrationRequestBody) : this(
        requestBody.userName!!,
        requestBody.firstName!!,
        requestBody.lastName!!,
        requestBody.email!!,
        requestBody.phoneNumber!!
    )
}