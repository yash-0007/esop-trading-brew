package com.esops.order

import com.esops.configuration.InventoryLimitConfiguration
import com.esops.configuration.PlatformFeesConfiguration
import com.esops.configuration.VestingConfiguration
import com.esops.configuration.WalletLimitConfiguration
import com.esops.e2e.CommonUtil
import com.esops.entity.EsopType
import com.esops.entity.OrderStatus
import com.esops.entity.OrderType
import com.esops.service.OrderService
import com.esops.service.PlatformService
import com.esops.service.UserService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigInteger

class OrderTest {
    private var vestingConfiguration = VestingConfiguration()
    private var inventoryLimitConfiguration = InventoryLimitConfiguration("0", "100000000000000000000")
    private var walletLimitConfiguration = WalletLimitConfiguration("0", "100000000000000000000")
    private var platformService = PlatformService()
    private var platformFeesConfiguration = PlatformFeesConfiguration(3F, 2F)

    private var userService = UserService(vestingConfiguration, inventoryLimitConfiguration, walletLimitConfiguration)

    private var orderService = OrderService(userService, platformService, platformFeesConfiguration)

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
        userService.addWalletMoney("john", commonUtil.addWalletMoneyRequestBody("500"))
        userService.addInventory("peter", commonUtil.addInventoryRequestBody(EsopType.NON_PERFORMANCE, "10"))
    }

    @AfterEach
    fun `tear down`() {
        userService.clearUsers()
        orderService.clearOrders()
    }

    @Test
    fun `should place an buy order`() {
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
        val buyOrderRequest = commonUtil.buyOrderRequest("10", "50")
        userService.canAddOrder("john", buyOrderRequest)

        val userPeter = userService.getUser("peter")
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
    fun `should match with existing non performance sell order`() {
        val userPeter = userService.getUser("peter")
        val sellOrderRequest = commonUtil.sellOrderRequest("10", "50", EsopType.NON_PERFORMANCE)
        userService.canAddOrder("peter", sellOrderRequest)

        val userJohn = userService.getUser("john")
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

    @Test
    fun `should match with existing performance sell order`() {
        val userPeter = userService.getUser("peter")
        userService.addInventory("peter", commonUtil.addInventoryRequestBody(EsopType.PERFORMANCE, "10"))
        val sellOrderRequest = commonUtil.sellOrderRequest("10", "50", EsopType.PERFORMANCE)
        userService.canAddOrder("peter", sellOrderRequest)

        val userJohn = userService.getUser("john")
        val buyOrderRequest = commonUtil.buyOrderRequest("10", "50")
        userService.canAddOrder("john", buyOrderRequest)

        val sellOrder = orderService.placeOrder("peter", sellOrderRequest)
        val buyOrder = orderService.placeOrder("john", buyOrderRequest)


        assertEquals(OrderStatus.COMPLETE, buyOrder.status)
        assertEquals(OrderStatus.COMPLETE, sellOrder.status)
        assertEquals(BigInteger.valueOf(10), userJohn.normal.free)
        assertEquals(BigInteger.valueOf(0), userPeter.performance.free)
        assertEquals(BigInteger.valueOf(0), userJohn.wallet.free)
        assertEquals(BigInteger.valueOf(490), userPeter.wallet.free) // platform fee 2%
    }

    @Test
    fun `should give preference to performance over non performance order`() {
        val userPeter = userService.getUser("peter")
        userService.addInventory("peter", commonUtil.addInventoryRequestBody(EsopType.PERFORMANCE, "10"))


        val nonPerformanceSellOrderRequest = commonUtil.sellOrderRequest("10", "50", EsopType.NON_PERFORMANCE)
        userService.canAddOrder("peter", nonPerformanceSellOrderRequest)
        val performanceSellOrderRequest = commonUtil.sellOrderRequest("10", "50", EsopType.PERFORMANCE)
        userService.canAddOrder("peter", performanceSellOrderRequest)

        val userJohn = userService.getUser("john")
        val buyOrderRequest = commonUtil.buyOrderRequest("10", "50")
        userService.canAddOrder("john", buyOrderRequest)

        val nonPerformanceSellOrder = orderService.placeOrder("peter", nonPerformanceSellOrderRequest)
        val performanceSellOrder = orderService.placeOrder("peter", performanceSellOrderRequest)
        val buyOrder = orderService.placeOrder("john", buyOrderRequest)

        assertEquals(OrderStatus.COMPLETE, buyOrder.status)
        assertEquals(OrderStatus.COMPLETE, performanceSellOrder.status)
        assertEquals(OrderStatus.PLACED, nonPerformanceSellOrder.status)
        assertEquals(BigInteger.valueOf(10), userJohn.normal.free)
        assertEquals(BigInteger.valueOf(0), userPeter.normal.free)
        assertEquals(BigInteger.valueOf(0), userJohn.wallet.free)
        assertEquals(BigInteger.valueOf(490), userPeter.wallet.free)
    }

    @Test
    fun `should match with multiple buy orders`() {
        val userJohn = userService.getUser("john")
        val buyOrderRequestOne = commonUtil.buyOrderRequest("5", "50")
        userService.canAddOrder("john", buyOrderRequestOne)

        val buyOrderRequestTwo = commonUtil.buyOrderRequest("5", "50")
        userService.canAddOrder("john", buyOrderRequestTwo)

        val userPeter = userService.getUser("peter")
        userService.addInventory("peter", commonUtil.addInventoryRequestBody(EsopType.PERFORMANCE, "10"))
        val sellOrderRequest = commonUtil.sellOrderRequest("10", "50", EsopType.PERFORMANCE)
        userService.canAddOrder("peter", sellOrderRequest)


        val buyOrderOne = orderService.placeOrder("john", buyOrderRequestOne)
        val buyOrderTwo = orderService.placeOrder("john", buyOrderRequestTwo)
        val sellOrder = orderService.placeOrder("peter", sellOrderRequest)


        assertEquals(OrderStatus.COMPLETE, buyOrderOne.status)
        assertEquals(OrderStatus.COMPLETE, buyOrderTwo.status)
        assertEquals(OrderStatus.COMPLETE, sellOrder.status)
        assertEquals(BigInteger.valueOf(10), userJohn.normal.free)
        assertEquals(BigInteger.valueOf(0), userPeter.performance.free)
        assertEquals(BigInteger.valueOf(0), userJohn.wallet.free)
        assertEquals(BigInteger.valueOf(490), userPeter.wallet.free) // platform fee 2%
    }

    @Test
    fun `should partially fill existing sell order`() {
        val userPeter = userService.getUser("peter")
        val sellOrderRequest = commonUtil.sellOrderRequest("10", "50", EsopType.NON_PERFORMANCE)
        userService.canAddOrder("peter", sellOrderRequest)

        val userJohn = userService.getUser("john")
        val buyOrderRequest = commonUtil.buyOrderRequest("5", "50")
        userService.canAddOrder("john", buyOrderRequest)

        val buyOrder = orderService.placeOrder("john", buyOrderRequest)
        val sellOrder = orderService.placeOrder("peter", sellOrderRequest)

        assertEquals(OrderStatus.PARTIAL, sellOrder.status)
        assertEquals(OrderStatus.COMPLETE, buyOrder.status)
        assertEquals(BigInteger.valueOf(5), userJohn.normal.free)
        assertEquals(BigInteger.valueOf(0), userPeter.normal.free)
        assertEquals(BigInteger.valueOf(5), userPeter.normal.locked)
        assertEquals(BigInteger.valueOf(250), userJohn.wallet.free)
        assertEquals(BigInteger.valueOf(243), userPeter.wallet.free) // platform fee 3%
    }

    @Test
    fun `should execute order for low sell and high buy`() {
        val userPeter = userService.getUser("peter")
        val sellOrderRequest = commonUtil.sellOrderRequest("10", "40", EsopType.NON_PERFORMANCE)
        userService.canAddOrder("peter", sellOrderRequest)


        val userJohn = userService.getUser("john")
        val buyOrderRequest = commonUtil.buyOrderRequest("10", "50")
        userService.canAddOrder("john", buyOrderRequest)


        val sellOrder = orderService.placeOrder("peter", sellOrderRequest)
        val buyOrder = orderService.placeOrder("john", buyOrderRequest)


        assertEquals(OrderStatus.COMPLETE, sellOrder.status)
        assertEquals(OrderStatus.COMPLETE, buyOrder.status)
        assertEquals(BigInteger.valueOf(10), userJohn.normal.free)
        assertEquals(BigInteger.valueOf(100), userJohn.wallet.free)
        assertEquals(BigInteger.valueOf(388), userPeter.wallet.free) // platform fee 3%
    }

    @Test
    fun `should not execute order for high sell and low buy`() {
        val userPeter = userService.getUser("peter")
        val sellOrderRequest = commonUtil.sellOrderRequest("10", "50", EsopType.NON_PERFORMANCE)
        userService.canAddOrder("peter", sellOrderRequest)


        val userJohn = userService.getUser("john")
        val buyOrderRequest = commonUtil.buyOrderRequest("10", "40")
        userService.canAddOrder("john", buyOrderRequest)


        val sellOrder = orderService.placeOrder("peter", sellOrderRequest)
        val buyOrder = orderService.placeOrder("john", buyOrderRequest)


        assertEquals(OrderStatus.PLACED, sellOrder.status)
        assertEquals(OrderStatus.PLACED, buyOrder.status)
        assertEquals(BigInteger.valueOf(10), userPeter.normal.locked)
        assertEquals(BigInteger.valueOf(400), userJohn.wallet.locked)

    }
}