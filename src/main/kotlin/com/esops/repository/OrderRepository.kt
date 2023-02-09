package com.esops.repository

import com.esops.entity.BuyOrderComparator
import com.esops.entity.Order
import com.esops.entity.SellOrderComparator
import jakarta.inject.Singleton
import java.util.*

@Singleton
class OrderRepository {

    private var orders = HashMap<String, HashMap<Long, Order>>()
    var buyOrderQueue = PriorityQueue(BuyOrderComparator)
    var sellOrderQueue = PriorityQueue(SellOrderComparator)
    private var orderIDCounter: Long = 0

    fun incrementOrderIDCounter() {
        orderIDCounter++
    }

    fun getOrderIDCounter(): Long {
        return orderIDCounter
    }

    fun initializeOrderMapIfEmpty(username: String) {
        if (orders[username].isNullOrEmpty())
            orders[username] = HashMap()
    }

    fun getOrderByUsername(username: String): List<Order> {
        return orders[username]!!.values.toList()
    }

    fun addOrder(username: String, order: Order) {
        orders[username]?.set(orderIDCounter, order)
    }

    fun clearOrders() {
        orders = HashMap()
        orderIDCounter = 0
        buyOrderQueue.clear()
        sellOrderQueue.clear()
    }

}