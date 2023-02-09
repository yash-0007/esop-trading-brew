package com.esops.configuration

import com.esops.validation.configuration.ValidMinMaxValueWalletLimit
import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("walletLimit")
@ValidMinMaxValueWalletLimit
class WalletLimitConfiguration(var min: String? = null, var max: String? = null)
