package com.esops.configuration

import com.esops.validation.configuration.ValidMinMaxValueInventoryLimit
import io.micronaut.context.annotation.ConfigurationProperties
import javax.validation.*

@ConfigurationProperties("inventoryLimit")
@ValidMinMaxValueInventoryLimit
class InventoryLimitConfiguration {
    var min: String? = null
    var max:  String? = null
}
