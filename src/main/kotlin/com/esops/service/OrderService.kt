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
            val minPrice = sellOrder.price
            val minQuantity =
                if (buyOrder.remainingQuantity < sellOrder.remainingQuantity) buyOrder.remainingQuantity else sellOrder.remainingQuantity
            if (minQuantity <= BigInteger.ZERO) return
            updateFilledFieldDuringMatching(sellOrder, buyOrder, minQuantity, minPrice)
            updateRemainingQuantityInOrderDuringMatching(sellOrder, minQuantity, buyOrder)
            val buyOrderValue = buyOrder.price.multiply(minQuantity)
            val sellOrderValue = sellOrder.price.multiply(minQuantity)
            buyOrderUser.wallet.free = buyOrderUser.wallet.free.add(buyOrderValue.subtract(sellOrderValue))
            buyOrderUser.wallet.locked = buyOrderUser.wallet.locked.subtract(buyOrderValue)

            buyOrderUser.normal.free = buyOrderUser.normal.free.add(minQuantity)
            when (sellOrder.esopType) {
                EsopType.PERFORMANCE -> {
                    sellOrderUser.performance.locked = sellOrderUser.performance.locked.subtract(minQuantity)
                    val platformFees: BigInteger =
                        sellOrderValue.multiply(
                            BigInteger(
                                (platformFeesConfiguration.performance * 100).toInt().toString()
                            )
                        )
                            .divide(BigInteger("10000"))
                    sellOrderUser.wallet.free = sellOrderUser.wallet.free.add(sellOrderValue).subtract(platformFees)
                    platformService.addPlatformFees(platformFees)
                }

                EsopType.NON_PERFORMANCE -> {
                    sellOrderUser.normal.locked =
                        sellOrderUser.normal.locked.subtract(minQuantity)
                    val platformFees: BigInteger =
                        sellOrderValue.multiply(BigInteger((platformFeesConfiguration.normal * 100).toInt().toString()))
                            .divide(BigInteger("10000"))
                    sellOrderUser.wallet.free = sellOrderUser.wallet.free.add(sellOrderValue).subtract(platformFees)
                    platformService.addPlatformFees(platformFees)
                }
            }
            updateOrderStatusDuringMatching(buyOrder, sellOrder)
        }
    }

    private fun updateOrderStatusDuringMatching(buyOrder: Order, sellOrder: Order) {
        when (buyOrder.remainingQuantity) {
            BigInteger("0") -> {
                buyOrder.status = OrderStatus.COMPLETE
            }

            else -> {
                buyOrder.status = OrderStatus.PARTIAL
            }
        }
        when (sellOrder.remainingQuantity) {
            BigInteger("0") -> {
                sellOrder.status = OrderStatus.COMPLETE
            }

            else -> {
                sellOrder.status = OrderStatus.PARTIAL
            }
        }
    }

    fun orderHistory(username: String): List<Order> {
        userService.testUser(username)
        return orderRepository.getOrderByUsername(username)
    }

}
