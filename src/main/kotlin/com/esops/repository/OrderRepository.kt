package com.esops.repository

import com.esops.entity.BuyOrderComparator
import com.esops.entity.Order
import com.esops.entity.SellOrderComparator
import jakarta.inject.Singleton
import java.util.*

@Singleton
class OrderRepository {

    var orders = HashMap<String, HashMap<Long, Order>>()
    var buyOrderQueue = PriorityQueue(BuyOrderComparator)
    var sellOrderQueue = PriorityQueue(SellOrderComparator)
    var orderIDCounter: Long = 0
}