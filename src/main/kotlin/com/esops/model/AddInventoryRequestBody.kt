package com.esops.model

import com.esops.entity.EsopType
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import com.esops.validation.ValidBigIntInventoryLimit

@Introspected
open class AddInventoryRequestBody {
    @NotBlank(message = "quantity field is blank")
    @ValidBigIntInventoryLimit
    var quantity: String? = null

    @Pattern(regexp = "^(PERFORMANCE|NON_PERFORMANCE)$", message = "type can be PERFORMANCE or NON_PERFORMANCE")
    var type: String = EsopType.NON_PERFORMANCE.toString()
}
