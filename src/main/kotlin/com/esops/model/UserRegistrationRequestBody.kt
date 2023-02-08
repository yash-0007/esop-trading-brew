package com.esops.model

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size


const val USERNAME_REGEX = ".*[a-zA-Z0-9].*"
const val NEGATED_HTTP_SPECIAL_CHARACTERS_REGEX = "[^$&+,/:;=?@ <>#%{}|^~\\[\\]`]+"
const val PHONE_REGEX =  "^(\\+[0-9]{1,3})?[1-9][0-9]{9}\$|^[1-9][0-9]{9}\$"
const val EMAIL_REGEX =
    "^(\"[a-zA-Z0-9@ ]{0,62}\"|[a-zA-Z0-9!#\$%&'*+\\-/=?^_`{|}~]([a-zA-Z0-9!#\$%&'*+\\-/=?^_`.{|}~]|\\\\ |\\\\@)+[a-zA-Z0-9!#\$%&'*+\\-/=?^_`{|}~])@[A-Za-z0-9-]+([\\-\\.]{1}[a-z0-9]+)*\\.[A-Za-z]{2,6}\$"

@Introspected
open class UserRegistrationRequestBody {
    @NotBlank(message = "firstName field is blank")
    var firstName: String? = null

    //@NotBlank(message = "lastName field is blank")
    var lastName: String? = ""

    @NotBlank(message = "userName field is blank")
    @Pattern(
        regexp = USERNAME_REGEX,
        message = "userName must contain one alphanumeric character"
    )@Pattern(
        regexp = NEGATED_HTTP_SPECIAL_CHARACTERS_REGEX,
        message = "userName cannot contain special characters \$&+,/:;=?@/s\"<>#%{}|\\^~[]`"
    )
    @Size(min=2, max=100, message="userName should have 2 to 100 characters")
    var userName: String? = null

    @NotBlank(message = "phoneNumber field is blank")
    @Pattern(regexp = PHONE_REGEX, message = "not a valid phoneNumber")
    var phoneNumber: String? = null

    @NotBlank(message = "email field is blank")
    @Email(regexp = EMAIL_REGEX, message = "email must be valid")
    var email: String? = null

}
