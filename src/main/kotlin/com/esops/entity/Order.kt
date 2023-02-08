package com.esops.entity

import java.math.BigInteger

enum class OrderType {
    BUY, SELL
}

enum class OrderStatus {
    PLACED, PARTIAL, COMPLETE
}

data class Order(
    val orderId: String,
    val username: String,
    val type: OrderType,
    val quantity: BigInteger,
    val price: BigInteger,
    val esopType: EsopType,
    val filled: MutableList<Filled> = mutableListOf(),
    var status: OrderStatus,
    var remainingQuantity: BigInteger,
    val createdAt: Long = System.currentTimeMillis()
)

data class Filled(
    val orderId: String,
    val quantity: BigInteger = BigInteger("0"),
    val price: BigInteger = BigInteger("0")
)

class BuyOrderComparator {
    companion object : Comparator<Order> {
        override fun compare(o1: Order, o2: Order): Int {
            val priceComparison = o2.price.compareTo(o1.price)
            val timeComparison = o1.createdAt.compareTo(o2.createdAt)
            if(priceComparison != 0) return priceComparison
            return timeComparison
        }
    }
}

class SellOrderComparator {
    companion object : Comparator<Order> {
        override fun compare(o1: Order, o2: Order): Int {
            val esopTypeComparison = o2.esopType.compareTo(o1.esopType)
            val priceComparison = o1.price.compareTo(o2.price)
            val timeComparison = o1.createdAt.compareTo(o2.createdAt)
            if(esopTypeComparison != 0) return esopTypeComparison
            if(priceComparison != 0) return priceComparison
            return timeComparison
        }
    }
}
