package com.esops.order

import com.esops.e2e.CommonUtil
import com.esops.entity.EsopType
import com.esops.entity.OrderStatus
import com.esops.entity.OrderType
import com.esops.service.OrderService
import com.esops.service.UserService
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigInteger

@MicronautTest
class OrderTest {

    @Inject
    lateinit var orderService: OrderService

    @Inject
    lateinit var userService: UserService

    private var commonUtil = CommonUtil()

    @BeforeEach
    fun `set up`() {
        userService.addUser(
            commonUtil.userRegistrationRequestBody(
                "John", "Doe", "john", "9524125143", "e2e2@gmail.com"
            )
        )
        userService.addUser(
            commonUtil.userRegistrationRequestBody(
                "Peter", "Parker", "peter", "9524125141", "e1e1@gmail.com"
            )
        )
    }

    @AfterEach
    fun `tear down`() {
        userService.clearUsers()
        orderService.clearOrders()
    }

    @Test
    fun `should place an buy order`() {
        userService.addWalletMoney("john", commonUtil.addWalletMoneyRequestBody("500"))
        val user = userService.getUser("john")
        val buyOrderRequest = commonUtil.buyOrderRequest("10", "50")
        userService.canAddOrder("john", buyOrderRequest)


        val buyOrder = orderService.placeOrder("john", buyOrderRequest)


        assertEquals(user.userName, buyOrder.username)
        assertEquals(BigInteger.valueOf(10), buyOrder.quantity)
        assertEquals("1", buyOrder.orderId)
        assertEquals(BigInteger.valueOf(50), buyOrder.price)
        assertEquals(OrderStatus.PLACED, buyOrder.status)
        assertEquals(OrderType.BUY, buyOrder.type)
        assertEquals(BigInteger.ZERO, user.wallet.free)
        assertEquals(BigInteger.valueOf(500), user.wallet.locked)
    }

    @Test
    fun `should place an performance sell order`() {
        userService.addInventory("john", commonUtil.addInventoryRequestBody(EsopType.PERFORMANCE, "10"))
        val user = userService.getUser("john")
        val sellOrderRequest = commonUtil.sellOrderRequest("10", "50", EsopType.PERFORMANCE)
        userService.canAddOrder("john", sellOrderRequest)

        val sellOrder = orderService.placeOrder("john", sellOrderRequest)

        assertEquals(user.userName, sellOrder.username)
        assertEquals(BigInteger.valueOf(10), sellOrder.quantity)
        assertEquals("1", sellOrder.orderId)
        assertEquals(BigInteger.valueOf(50), sellOrder.price)
        assertEquals(OrderStatus.PLACED, sellOrder.status)
        assertEquals(OrderType.SELL, sellOrder.type)
        assertEquals(EsopType.PERFORMANCE, sellOrder.esopType)
        assertEquals(BigInteger.ZERO, user.performance.free)
        assertEquals(BigInteger.valueOf(10), user.performance.locked)
    }

    @Test
    fun `should place an non-performance sell order`() {
        userService.addInventory("john", commonUtil.addInventoryRequestBody(EsopType.NON_PERFORMANCE, "10"))
        val sellOrderRequest = commonUtil.sellOrderRequest("10", "50", EsopType.NON_PERFORMANCE)
        userService.canAddOrder("john", sellOrderRequest)

        val sellOrder = orderService.placeOrder("john", sellOrderRequest)

        val user = userService.getUser("john")

        assertEquals(user.userName, sellOrder.username)
        assertEquals(BigInteger.valueOf(10), sellOrder.quantity)
        assertEquals("1", sellOrder.orderId)
        assertEquals(BigInteger.valueOf(50), sellOrder.price)
        assertEquals(OrderStatus.PLACED, sellOrder.status)
        assertEquals(OrderType.SELL, sellOrder.type)
        assertEquals(EsopType.NON_PERFORMANCE, sellOrder.esopType)
        assertEquals(BigInteger.ZERO, user.normal.free)
        assertEquals(BigInteger.valueOf(10), user.normal.locked)
    }

    @Test
    fun `should match with existing buy order`() {
        val userJohn = userService.getUser("john")
        userService.addWalletMoney("john", commonUtil.addWalletMoneyRequestBody("500"))
        val buyOrderRequest = commonUtil.buyOrderRequest("10", "50")
        userService.canAddOrder("john", buyOrderRequest)

        val userPeter = userService.getUser("peter")
        userService.addInventory("peter", commonUtil.addInventoryRequestBody(EsopType.NON_PERFORMANCE, "10"))
        val sellOrderRequest = commonUtil.sellOrderRequest("10", "50", EsopType.NON_PERFORMANCE)
        userService.canAddOrder("peter", sellOrderRequest)

        val buyOrder = orderService.placeOrder("john", buyOrderRequest)
        val sellOrder = orderService.placeOrder("peter", sellOrderRequest)


        assertEquals(OrderStatus.COMPLETE, buyOrder.status)
        assertEquals(OrderStatus.COMPLETE, sellOrder.status)
        assertEquals(BigInteger.valueOf(10), userJohn.normal.free)
        assertEquals(BigInteger.valueOf(0), userPeter.normal.free)
        assertEquals(BigInteger.valueOf(0), userJohn.wallet.free)
        assertEquals(BigInteger.valueOf(485), userPeter.wallet.free) // platform fee 3%
    }

    @Test
    fun `should match with existing sell order`() {
        val userPeter = userService.getUser("peter")
        userService.addInventory("peter", commonUtil.addInventoryRequestBody(EsopType.NON_PERFORMANCE, "10"))
        val sellOrderRequest = commonUtil.sellOrderRequest("10", "50", EsopType.NON_PERFORMANCE)
        userService.canAddOrder("peter", sellOrderRequest)

        val userJohn = userService.getUser("john")
        userService.addWalletMoney("john", commonUtil.addWalletMoneyRequestBody("500"))
        val buyOrderRequest = commonUtil.buyOrderRequest("10", "50")
        userService.canAddOrder("john", buyOrderRequest)

        val sellOrder = orderService.placeOrder("peter", sellOrderRequest)
        val buyOrder = orderService.placeOrder("john", buyOrderRequest)


        assertEquals(OrderStatus.COMPLETE, buyOrder.status)
        assertEquals(OrderStatus.COMPLETE, sellOrder.status)
        assertEquals(BigInteger.valueOf(10), userJohn.normal.free)
        assertEquals(BigInteger.valueOf(0), userPeter.normal.free)
        assertEquals(BigInteger.valueOf(0), userJohn.wallet.free)
        assertEquals(BigInteger.valueOf(485), userPeter.wallet.free) // platform fee 3%
    }
}