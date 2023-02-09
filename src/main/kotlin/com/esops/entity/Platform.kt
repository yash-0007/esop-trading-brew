package com.esops.entity

import java.math.BigInteger

class Platform {
    private var totalFeesCollected = BigInteger.ZERO
    fun add(fee: BigInteger) {
        totalFeesCollected = this.totalFeesCollected.add(fee)
    }

    fun getTotalFessCollected(): BigInteger {
        return totalFeesCollected
    }
}
