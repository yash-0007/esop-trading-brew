package com.esops.service

import com.esops.configuration.PlatformFeesConfiguration
import com.esops.entity.*
import com.esops.model.AddOrderRequestBody
import com.esops.repository.OrderRepository
import jakarta.inject.Singleton
import java.math.BigInteger

@Singleton
class OrderService(
        private var userService: UserService,
        private var platformService: PlatformService,
        private var platformFeesConfiguration: PlatformFeesConfiguration,
        private var orderRepository: OrderRepository
) {


    fun placeOrder(username: String, addOrderRequestBody: AddOrderRequestBody): Order {
        userService.checkOrderPlacement(username, addOrderRequestBody)
        val user = this.userService.getUser(username)

        return when (OrderType.valueOf(addOrderRequestBody.type!!)) {
            OrderType.BUY -> placeBuyOrder(addOrderRequestBody, user)
            OrderType.SELL -> placeSellOrder(addOrderRequestBody, user)
        }
    }

    private fun placeBuyOrder(
            addOrderRequestBody: AddOrderRequestBody,
            user: User
    ): Order {
        val username = user.userName
        val price = BigInteger(addOrderRequestBody.price!!)
        val quantity = BigInteger(addOrderRequestBody.quantity!!)
        val orderValue = price.multiply(quantity)
        val order = Order(
                orderRepository.getOrderIDCounter().toString(),
                username,
                OrderType.BUY,
                quantity,
                price,
                EsopType.NON_PERFORMANCE,
                status = OrderStatus.PLACED,
                remainingQuantity = quantity
        )
        orderRepository.addOrder(username, order)
        orderRepository.addToOrderQueue(OrderType.BUY, order)
        user.moveWalletMoneyFromFreeToLockedState(orderValue)
        executeBuyOrder(order)
        return order
    }

    private fun executeBuyOrder(buyOrder: Order) {
        val buyOrderUser = userService.getUser(buyOrder.username)
        val sellOrderQueue = orderRepository.getSellOrderQueue()
        for (sellOrder in sellOrderQueue) {
            val sellOrderUser = userService.getUser(sellOrder.username)
            applyOrderMatchingAlgorithm(buyOrder, sellOrder, buyOrderUser, sellOrderUser)
            if (buyOrder.remainingQuantity == BigInteger("0")) {
                break
            }
        }
        orderRepository.setOrderQueue(OrderType.SELL, sellOrderQueue)
    }

    private fun updateRemainingQuantityInOrderDuringMatching(
            sellOrder: Order,
            minQuantity: BigInteger,
            buyOrder: Order
    ) {
        sellOrder.remainingQuantity = sellOrder.remainingQuantity.subtract(minQuantity)
        buyOrder.remainingQuantity = buyOrder.remainingQuantity.subtract(minQuantity)
    }

    private fun updateFilledFieldDuringMatching(
            sellOrder: Order,
            buyOrder: Order,
            minQuantity: BigInteger,
            minPrice: BigInteger
    ) {
        sellOrder.filled.add(Filled(buyOrder.orderId, minQuantity, minPrice))
        buyOrder.filled.add(Filled(sellOrder.orderId, minQuantity, minPrice))
    }

    private fun placeSellOrder(
            addOrderRequestBody: AddOrderRequestBody,
            user: User
    ): Order {
        val username = user.userName
        val esopType = EsopType.valueOf(addOrderRequestBody.esopType!!)
        val price = BigInteger(addOrderRequestBody.price!!)
        val quantity = BigInteger(addOrderRequestBody.quantity!!)
        val order = Order(
                orderRepository.getOrderIDCounter().toString(),
                username,
                OrderType.SELL,
                quantity,
                price,
                esopType,
                status = OrderStatus.PLACED,
                remainingQuantity = quantity
        )
        orderRepository.addOrder(username, order)
        orderRepository.addToOrderQueue(OrderType.SELL, order)
        user.moveInventoryFromFreeToLockedState(esopType, quantity)
        executeSellOrder(order)
        return order
    }

    private fun executeSellOrder(sellOrder: Order) {
        val sellOrderUser = userService.getUser(sellOrder.username)
        val buyOrderQueue = orderRepository.getBuyOrderQueue()
        for (buyOrder in buyOrderQueue) {
            val buyOrderUser = userService.getUser(buyOrder.username)
            applyOrderMatchingAlgorithm(buyOrder, sellOrder, buyOrderUser, sellOrderUser)
            if (sellOrder.remainingQuantity == BigInteger("0")) {
                break
            }
        }
        orderRepository.setOrderQueue(OrderType.BUY, buyOrderQueue)
        orderRepository.cleanQueue()
    }

    private fun applyOrderMatchingAlgorithm(
            buyOrder: Order,
            sellOrder: Order,
            buyOrderUser: User,
            sellOrderUser: User
    ) {
        if (buyOrder.price >= sellOrder.price) {
            val platformFees: BigInteger
            val minPrice = sellOrder.price
            val minQuantity = calculateMinQuantity(buyOrder, sellOrder)
            if (minQuantity <= BigInteger.ZERO) return

            updateFilledFieldDuringMatching(sellOrder, buyOrder, minQuantity, minPrice)
            updateRemainingQuantityInOrderDuringMatching(sellOrder, minQuantity, buyOrder)

            val buyOrderValue = buyOrder.price * minQuantity
            val sellOrderValue = sellOrder.price * minQuantity

            platformFees = when (sellOrder.esopType) {
                EsopType.PERFORMANCE ->
                    calculatePerformancePlatformFees(sellOrderUser, minQuantity, sellOrderValue)

                EsopType.NON_PERFORMANCE ->
                    calculateNormalPlatformFees(sellOrderUser, minQuantity, sellOrderValue)
            }

            updateBuyerWallet(buyOrderUser, buyOrderValue, sellOrderValue)

            buyOrderUser.normal.free += minQuantity
            sellOrderUser.wallet.free += (sellOrderValue - platformFees)

            platformService.addPlatformFees(platformFees)

            updateOrderStatus(buyOrder)
            updateOrderStatus(sellOrder)
        }
    }

    private fun updateBuyerWallet(
            buyOrderUser: User,
            buyOrderValue: BigInteger,
            sellOrderValue: BigInteger
    ) {
        buyOrderUser.wallet.free += (buyOrderValue - sellOrderValue)
        buyOrderUser.wallet.locked -= buyOrderValue
    }

    private fun calculateMinQuantity(buyOrder: Order, sellOrder: Order) =
            if (buyOrder.remainingQuantity < sellOrder.remainingQuantity) buyOrder.remainingQuantity
            else sellOrder.remainingQuantity

    private fun calculateNormalPlatformFees(
            sellOrderUser: User,
            minQuantity: BigInteger,
            sellOrderValue: BigInteger
    ): BigInteger {
        sellOrderUser.normal.locked -= minQuantity
        return sellOrderValue.multiply(BigInteger((platformFeesConfiguration.normal * 100).toInt().toString()))
                .divide(BigInteger("10000"))
    }

    private fun calculatePerformancePlatformFees(
            sellOrderUser: User,
            minQuantity: BigInteger,
            sellOrderValue: BigInteger
    ): BigInteger {
        sellOrderUser.performance.locked -= minQuantity
        return sellOrderValue.multiply(BigInteger((platformFeesConfiguration.performance * 100).toInt().toString()))
                .divide(BigInteger("10000"))
    }

    private fun updateOrderStatus(order: Order) {
        if (order.remainingQuantity == BigInteger.ZERO) order.status = OrderStatus.COMPLETE
        else order.status = OrderStatus.PARTIAL
    }

    fun orderHistory(username: String): List<Order> {
        userService.testUser(username)
        return orderRepository.getOrderByUsername(username)
    }

}
