package com.esops.repository

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OrderRepositoryTest{

    private val orderRepository = OrderRepository()

    @BeforeEach
    fun `set up`(){
        orderRepository.clearOrders()
    }

    @Test
    fun `should get order ID counter`() {
        val orderID = orderRepository.getOrderIDCounter()

        assertEquals(0, orderID)
    }

    @Test
    fun `should increment order ID counter`() {
        val orderID = orderRepository.getOrderIDCounter()

        orderRepository.incrementOrderIDCounter()

        assertEquals(orderID + 1, orderRepository.getOrderIDCounter())
    }
}