package com.esops.service

import com.esops.configuration.InventoryLimitConfiguration
import com.esops.configuration.VestingConfiguration
import com.esops.configuration.WalletLimitConfiguration
import com.esops.entity.*
import com.esops.exception.InventoryLimitExceededException
import com.esops.exception.UserNotFoundException
import com.esops.exception.UserNotUniqueException
import com.esops.exception.WalletLimitExceededException
import com.esops.model.*
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

@Singleton
class UserService {

    private var users = HashMap<String, User>()

    @Inject
    lateinit var vestingConfiguration: VestingConfiguration

    @Inject
    private lateinit var inventoryLimitConfiguration: InventoryLimitConfiguration

    @Inject
    private lateinit var walletLimitConfiguration: WalletLimitConfiguration

    fun addUser(userRegistrationRequestBody: UserRegistrationRequestBody): UserRegistrationResponseBody {
        checkUniqueness(userRegistrationRequestBody)
        val firstName = userRegistrationRequestBody.firstName
        val lastName = userRegistrationRequestBody.lastName
        val userName = userRegistrationRequestBody.userName
        val phoneNumber = userRegistrationRequestBody.phoneNumber
        val email = userRegistrationRequestBody.email
        users[userName!!] = User(firstName!!, lastName!!, userName, email!!, phoneNumber!!)
        return UserRegistrationResponseBody(userRegistrationRequestBody)
    }

    fun clearUsers() {
        users = HashMap()
    }

    fun getUser(username: String): User {
        return users[username]!!
    }

    private fun checkUniqueness(userRegistrationRequestBody: UserRegistrationRequestBody) {
        val userName = userRegistrationRequestBody.userName
        val phoneNumber = userRegistrationRequestBody.phoneNumber
        val email = userRegistrationRequestBody.email
        val error = mutableListOf<String>()
        if (users.containsKey(userName)) {
            error.add("userName already exists")
        }
        for ((_, user) in users) {
            if (user.email == email) {
                error.add("email already exists")
            }
        }
        for ((_, user) in users) {
            if (user.phoneNumber == phoneNumber) {
                error.add("phoneNumber already exists")
            }
        }
        if (error.isNotEmpty()) throw UserNotUniqueException(error)
    }

    fun testUser(username: String) {
        if (users.containsKey(username)) {
            return
        }
        throw UserNotFoundException(listOf("user does not exists"))
    }

    fun accountInformation(username: String): FormattedUser {
        testUser(username)
        updateUserUnvestedInventory(username, vestingConfiguration.duration)
        return users[username]!!.getFormatterUserData(vestingConfiguration.duration)
    }

    private fun getNormalInventory(user: User): BigInteger {
        return user.normal.free + user.normal.locked
    }

    private fun getPerformanceInventory(user: User): BigInteger {
        return user.performance.free + user.performance.locked
    }

    private fun getUnvestedESOPs(user: User): BigInteger {
        var totalUnvestedESOPs = BigInteger("0")
        for (i in user.unvestedInventoryList) {
            for (j in 0 until i.dividedInventory.size) {
                totalUnvestedESOPs += i.dividedInventory[j]
            }
        }
        return totalUnvestedESOPs
    }

    private fun getWalletAmount(user: User): BigInteger {
        return user.wallet.free + user.wallet.locked
    }

    private fun getTotalInventory(user: User): BigInteger {
        return getNormalInventory(user) + getPerformanceInventory(user) + getUnvestedESOPs(user)
    }

    fun canAddInventory(username: String, addInventoryRequestBody: AddInventoryRequestBody) {
        testUser(username)
        val error = mutableListOf<String>()
        updateUserUnvestedInventory(username, vestingConfiguration.duration)

        val user = users[username]!!
        if (getTotalInventory(user).add(
                BigInteger(
                    addInventoryRequestBody.quantity!!
                )
            ) > inventoryLimitConfiguration.max!!.toBigInteger()
        )
            throw InventoryLimitExceededException(listOf("Inventory limit (${inventoryLimitConfiguration.max}) exceeded"))
    }

    private fun canAddWalletMoney(username: String, addWalletMoneyRequestBody: AddWalletMoneyRequestBody) {
        testUser(username)
        if (getWalletAmount(users[username]!!) +
            (BigInteger(
                addWalletMoneyRequestBody.amount!!
            )
                    ) > BigInteger(walletLimitConfiguration.max!!)
        ) {
            throw WalletLimitExceededException(listOf("Total Wallet limit (${walletLimitConfiguration.max}) exceeded"))
        }
    }

    fun addInventory(username: String, addInventoryRequestBody: AddInventoryRequestBody): AddInventoryResponseBody {
        canAddInventory(username, addInventoryRequestBody)
        return when (EsopType.valueOf(addInventoryRequestBody.type)) {
            EsopType.NON_PERFORMANCE -> {
                val unvestedInventory =
                    UnvestedInventory(dividedInventory = breakIntoVestingCycles(addInventoryRequestBody.quantity!!))
                users[username]!!.unvestedInventoryList.add(unvestedInventory)
                AddInventoryResponseBody("${addInventoryRequestBody.quantity} ESOPs added to your account")
            }

            EsopType.PERFORMANCE -> {
                users[username]!!.performance.free =
                    users[username]!!.performance.free.add(BigInteger(addInventoryRequestBody.quantity!!))
                AddInventoryResponseBody("${addInventoryRequestBody.quantity} ${addInventoryRequestBody.type} ESOPs added to your account")
            }
        }
    }

    private fun breakIntoVestingCycles(quantity: String): ArrayList<BigInteger> {
        val floatCycles = arrayListOf<BigDecimal>()
        val concreteCycles = arrayListOf<BigInteger>()
        for (cycle in vestingConfiguration.breakup) {
            floatCycles.add(cycle.toBigDecimal() * quantity.toBigDecimal())
        }
        var decimalSum = BigDecimal("0")
        var concreteSum = BigInteger("0")
        for (cycle in floatCycles) {
            val currentCycle = cycle + decimalSum - concreteSum.toBigDecimal()
            val currentCycleFloor = currentCycle.round(MathContext(0, RoundingMode.FLOOR)).toBigInteger()
            concreteCycles.add(currentCycleFloor)
            decimalSum += cycle
            concreteSum += currentCycleFloor
        }
        return concreteCycles
    }


    fun addWalletMoney(
        username: String,
        addWalletMoneyRequestBody: AddWalletMoneyRequestBody
    ): AddWalletMoneyResponseBody {
        canAddWalletMoney(username, addWalletMoneyRequestBody)
        users[username]!!.wallet.free =
            users[username]!!.wallet.free.add(BigInteger(addWalletMoneyRequestBody.amount!!))
        return AddWalletMoneyResponseBody("${addWalletMoneyRequestBody.amount} amount added to your account")
    }

    fun canAddOrder(username: String, addOrderRequestBody: AddOrderRequestBody): List<String?> {
        testUser(username)
        val error = mutableListOf<String>()
        val user = users[username]!!

        updateUserUnvestedInventory(username, vestingConfiguration.duration)

        when (addOrderRequestBody.type) {
            OrderType.BUY.toString() -> {
                val resultCheckSufficientWalletMoneyDuringBuy =
                    checkSufficientWalletMoneyDuringBuy(
                        user,
                        addOrderRequestBody.quantity,
                        addOrderRequestBody.price
                    )
                if (!resultCheckSufficientWalletMoneyDuringBuy)
                    error.add("insufficient wallet funds")
                if (resultCheckSufficientWalletMoneyDuringBuy && !checkSufficientInventorySpace(
                        user,
                        addOrderRequestBody.quantity
                    )
                )
                    error.add("not enough free space for new stocks to be added to inventory")
            }

            OrderType.SELL.toString() -> {
                val resultCheckSufficientInventoryDuringSell = checkSufficientInventoryDuringSell(
                    user,
                    addOrderRequestBody.quantity,
                    addOrderRequestBody.esopType
                )
                if (!resultCheckSufficientInventoryDuringSell)
                    error.add("insufficient inventory")
                if (resultCheckSufficientInventoryDuringSell && !checkSufficientWalletMoneySpace(
                        user,
                        addOrderRequestBody.quantity,
                        addOrderRequestBody.price
                    )
                )
                    error.add("not enough space in wallet to add money")
            }
        }
        return error
    }

    private fun checkSufficientInventorySpace(user: User, quantity: String?): Boolean {
        return getTotalInventory(user) + quantity!!.toBigInteger() <= BigInteger(inventoryLimitConfiguration.max!!)
    }

    private fun checkSufficientWalletMoneySpace(user: User, quantity: String?, price: String?): Boolean {
        val orderValue = BigInteger(quantity!!).multiply(BigInteger(price!!))
        return user.wallet.free + user.wallet.locked + orderValue <= BigInteger(walletLimitConfiguration.max!!)
    }

    private fun checkSufficientInventoryDuringSell(user: User, quantity: String?, esopType: String?): Boolean {
        return when (esopType) {
            EsopType.NON_PERFORMANCE.toString() -> {
                user.normal.free >= BigInteger(quantity!!)
            }

            EsopType.PERFORMANCE.toString() -> {
                user.performance.free >= BigInteger(quantity!!)
            }

            else -> false
        }
    }

    private fun checkSufficientWalletMoneyDuringBuy(user: User, quantity: String?, price: String?): Boolean {
        return user.wallet.free >= BigInteger(quantity!!).multiply(BigInteger(price!!))
    }

    private fun updateUserUnvestedInventory(username: String, duration: Int) {
        val user = users[username]!!
        val unVestedInventoryList = user.unvestedInventoryList
        val currentTime = System.currentTimeMillis()
//        val nanoSecsIn1Day = 86400000000000
        val milliSecsIn1Sec = 1000
        for (i in unVestedInventoryList) {
//        val timeDiffInDays: Int = ((currentTime - i.addedAt) / nanoSecsIn1Day).toInt()
            val timeDiffInSecs: Int = ((currentTime - i.addedAt) / milliSecsIn1Sec).toInt()
            for (day in 0 until i.dividedInventory.size) {
                if ((day + 1) * duration < timeDiffInSecs) {
                    user.normal.free += i.dividedInventory[day]
                    i.dividedInventory[day] = BigInteger("0")
                }
            }
        }
    }
}
