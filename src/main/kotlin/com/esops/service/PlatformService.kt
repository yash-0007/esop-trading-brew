package com.esops.service

import com.esops.entity.Platform
import jakarta.inject.Singleton
import java.math.BigInteger

@Singleton
class PlatformService {
    private val platform = Platform()
    fun getCollectedPlatformFee(): Map<String, BigInteger> {
        return mapOf("feesCollected" to platform.getTotalFessCollected())
    }

    fun addPlatformFees(fee: BigInteger) {
        platform.add(fee)
    }
}
