package com.esops.repository

import com.esops.entity.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigInteger

class OrderRepositoryTest {

    private val orderRepository = OrderRepository()

    @BeforeEach
    fun `set up`() {
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

    @Test
    fun `should initialize empty order map for user`() {
        orderRepository.initializeOrderMapIfEmpty("ramesh")

        assertEquals(listOf<Order>(), orderRepository.getOrderByUsername("ramesh"))
    }

    @Test
    fun `should get order list for user`() {
        orderRepository.initializeOrderMapIfEmpty("ramesh")
        orderRepository.addOrder(
            "ramesh", Order(
                "1",
                "ramesh",
                OrderType.BUY,
                BigInteger.valueOf(10),
                BigInteger.valueOf(10),
                EsopType.NON_PERFORMANCE,
                mutableListOf(),
                OrderStatus.PLACED,
                BigInteger.valueOf(10),
            )
        )

        assertEquals("1", orderRepository.getOrderByUsername("ramesh")[0].orderId)
        assertEquals("ramesh", orderRepository.getOrderByUsername("ramesh")[0].username)
    }
}