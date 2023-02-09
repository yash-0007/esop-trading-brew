package com.esops.configuration

import com.esops.validation.configuration.ValidMinMaxValueWalletLimit
import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("walletLimit")
@ValidMinMaxValueWalletLimit
class WalletLimitConfiguration(var min: String = "", var max: String = "")
