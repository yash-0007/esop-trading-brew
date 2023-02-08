package com.esops.controller

import com.esops.configuration.InventoryLimitConfiguration
import com.esops.configuration.PlatformFeesConfiguration
import com.esops.configuration.VestingConfiguration
import com.esops.configuration.WalletLimitConfiguration
import com.esops.model.PlatformConfigurationResponse
import com.esops.service.PlatformService
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Produces
import io.micronaut.http.annotation.Status
import jakarta.inject.Inject

@Controller("/platform")
class PlatformController {

    @Inject
    lateinit var platformFeesConfiguration: PlatformFeesConfiguration

    @Inject
    lateinit var inventoryLimitConfiguration: InventoryLimitConfiguration

    @Inject
    lateinit var walletLimitConfiguration: WalletLimitConfiguration

    @Inject
    lateinit var vestingConfiguration: VestingConfiguration

    @Inject
    lateinit var platformService: PlatformService

    @Get(uri = "/fees")
    @Produces(MediaType.APPLICATION_JSON)
    fun fees(): HttpResponse<Any> {
        return HttpResponse.ok(platformService.getCollectedPlatformFee())
    }

    @Get(uri = "/configuration")
    @Status(HttpStatus.OK)
    @Produces(MediaType.APPLICATION_JSON)
    fun configuration(): PlatformConfigurationResponse {
        return PlatformConfigurationResponse(
            inventoryLimitConfiguration,
            platformFeesConfiguration,
            vestingConfiguration,
            walletLimitConfiguration
        )
    }
}
