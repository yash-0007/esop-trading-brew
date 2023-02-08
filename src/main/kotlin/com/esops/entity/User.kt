package com.esops.entity

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigInteger
import java.util.*


enum class EsopType: Comparable<EsopType> {
    NON_PERFORMANCE, PERFORMANCE
}

data class Inventory(val type: EsopType, var free: BigInteger = BigInteger("0"), var locked: BigInteger = BigInteger("0"))
data class Wallet(var free: BigInteger = BigInteger("0"), var locked: BigInteger = BigInteger("0"))


data class UnvestedInventory(
    var addedAt: Long = System.currentTimeMillis(),
    var dividedInventory: MutableList<BigInteger>
)

data class UnvestedInventoryResponse(
    var time: String,
    var amount: BigInteger = BigInteger("0")
)
data class User(
    val firstName: String,
    val lastName: String,
    val userName: String,
    val email: String,
    val phoneNumber: String,
    var wallet: Wallet = Wallet(),
    val normal: Inventory = Inventory(EsopType.NON_PERFORMANCE),
    val performance: Inventory = Inventory(EsopType.PERFORMANCE),
    var unvestedInventoryList: MutableList<UnvestedInventory> = mutableListOf<UnvestedInventory>()

) {

    fun getFormatterUserData(vestingDuration: Int): FormattedUser {
        return FormattedUser(
            firstName,
            lastName,
            userName,
            email,
            phoneNumber,
            wallet,
            listOf(normal, performance),
            getUnvestedInventoryListResponse(unvestedInventoryList, vestingDuration)
        )
    }

    fun moveWalletMoneyFromFreeToLockedState(price: BigInteger) {
        wallet.free = BigInteger(wallet.free.toString()).subtract(price)
        wallet.locked = BigInteger(wallet.locked.toString()).add(price)
    }

    fun moveInventoryFromFreeToLockedState(esopType: EsopType, price: BigInteger) {
        when (esopType) {
            EsopType.NON_PERFORMANCE -> {
                normal.free = BigInteger(normal.free.toString()).subtract(price)
                normal.locked = BigInteger(normal.locked.toString()).add(price)
            }
            EsopType.PERFORMANCE -> {
                performance.free = BigInteger(performance.free.toString()).subtract(price)
                performance.locked = BigInteger(performance.locked.toString()).add(price)
            }
        }
    }

    private fun getUnvestedInventoryListResponse(unvestedInventoryList: MutableList<UnvestedInventory>, vestingDuration: Int): MutableList<UnvestedInventoryResponse> {
        val unvestedInventoryResponseList = mutableListOf<UnvestedInventoryResponse>()
        for( inventory in unvestedInventoryList) {
            for(day in 0 until inventory.dividedInventory.size){
                if(inventory.dividedInventory[day]!= BigInteger("0")) {
                    val element = UnvestedInventoryResponse(
                        addSecsToDate(Date(inventory.addedAt), (day + 1) * vestingDuration).toString(),
                        inventory.dividedInventory[day]
                    )
                    unvestedInventoryResponseList.add(element)
                }
            }
        }
        return unvestedInventoryResponseList
    }

    private fun addHoursToDate(date: Date?, hours: Int): Date? {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.HOUR_OF_DAY, hours)
        return calendar.time
    }

    private fun addSecsToDate(date: Date?, secs: Int): Date? {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.SECOND, secs)
        return calendar.time
    }
}

@JsonInclude(value = JsonInclude.Include.NON_NULL)
data class FormattedUser(
    val firstName: String,
    val lastName: String,
    val userName: String,
    val email: String,
    val phoneNumber: String,
    val wallet: Wallet,
    val inventory: List<Inventory>,
    val unvestedInventoryList: List<UnvestedInventoryResponse>
)
