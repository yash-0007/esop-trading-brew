package com.esops.repository

import com.esops.entity.BuyOrderComparator
import com.esops.entity.Order
import com.esops.entity.OrderType
import com.esops.entity.SellOrderComparator
import jakarta.inject.Singleton
import java.math.BigInteger
import java.util.*

@Singleton
class OrderRepository {

    private var orders = HashMap<String, HashMap<Long, Order>>()
    private var buyOrderQueue = PriorityQueue(BuyOrderComparator)
    private var sellOrderQueue = PriorityQueue(SellOrderComparator)
    private var orderIDCounter: Long = 1

    fun getOrderIDCounter(): Long {
        return orderIDCounter
    }

    private fun incrementOrderIDCounter() {
        orderIDCounter++
    }

    private fun initializeOrderMapIfEmpty(username: String) {
        if (orders[username].isNullOrEmpty())
            orders[username] = HashMap()
    }

    fun getOrderByUsername(username: String): List<Order> {
        initializeOrderMapIfEmpty(username)
        return orders[username]!!.values.toList()
    }

    fun addOrder(username: String, order: Order) {
        initializeOrderMapIfEmpty(username)
        incrementOrderIDCounter()
        orders[username]?.set(orderIDCounter, order)
    }

    fun getSellOrderQueue(): PriorityQueue<Order> {
        return sellOrderQueue
    }

    fun getBuyOrderQueue(): PriorityQueue<Order> {
        return buyOrderQueue
    }

    fun setOrderQueue(type: OrderType, orderQueue: PriorityQueue<Order>) {
        if (type == OrderType.BUY) buyOrderQueue = orderQueue
        if (type == OrderType.SELL) sellOrderQueue = orderQueue
    }

    fun addToOrderQueue(type: OrderType, order: Order) {
        if (type == OrderType.BUY) buyOrderQueue.add(order)
        if (type == OrderType.SELL) sellOrderQueue.add(order)
    }

    fun cleanQueue() {
        buyOrderQueue.removeIf { it.remainingQuantity == BigInteger("0") }
        sellOrderQueue.removeIf { it.remainingQuantity == BigInteger("0") }
    }

    fun clearOrders() {
        orders = HashMap()
        orderIDCounter = 1
        buyOrderQueue.clear()
        sellOrderQueue.clear()
    }

}