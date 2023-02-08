package com.esops.model

import com.esops.validation.ValidBigIntWalletLimit
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
open class AddWalletMoneyRequestBody {
    @ValidBigIntWalletLimit
    @NotBlank(message = "amount field is blank")
    var amount: String? = null
}
