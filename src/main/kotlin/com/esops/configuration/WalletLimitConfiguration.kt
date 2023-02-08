package com.esops.configuration

import io.micronaut.context.annotation.ConfigurationProperties
import com.esops.validation.configuration.ValidMinMaxValueWalletLimit

@ConfigurationProperties("walletLimit")
@ValidMinMaxValueWalletLimit
class WalletLimitConfiguration {
    var min: String? = null
    var max: String? = null
}
