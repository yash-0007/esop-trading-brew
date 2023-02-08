package com.esops.model

import com.esops.entity.EsopType
import com.esops.entity.OrderType
import com.esops.validation.ValidBigIntInventoryLimit
import com.esops.validation.ValidBigIntWalletLimit
import com.esops.validation.ValidOrder
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern


@ValidOrder
@Introspected
open class AddOrderRequestBody {
    @NotBlank(message = "type field is blank")
    @Pattern(regexp = "^(BUY|SELL)$", message = "order type must be of type buy or sell")
    var type: String? = null

    @NotBlank(message = "quantity field is blank")
    @ValidBigIntInventoryLimit
    var quantity: String? = null

    @NotBlank(message = "price field is blank")
    @ValidBigIntWalletLimit
    var price: String? = null

    var esopType: String? = null

}
