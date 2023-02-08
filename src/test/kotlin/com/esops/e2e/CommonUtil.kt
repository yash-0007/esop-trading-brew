package com.esops.e2e

import com.esops.entity.EsopType
import com.esops.entity.OrderType
import com.esops.model.AddInventoryRequestBody
import com.esops.model.AddOrderRequestBody
import com.esops.model.AddWalletMoneyRequestBody
import com.esops.model.UserRegistrationRequestBody

class CommonUtil {
    fun addInventoryRequestBody(esopType: EsopType, quantity: String): AddInventoryRequestBody {
        val dummyInventoryRequestBody = AddInventoryRequestBody()
        dummyInventoryRequestBody.type = esopType.toString()
        dummyInventoryRequestBody.quantity = quantity
        return dummyInventoryRequestBody
    }

    fun userRegistrationRequestBody(
        firstName: String = "John",
        lastName: String = "Doe",
        userName: String = "john",
        phoneNumber: String = "1234567890",
        email: String = "john@john.com"
    ): UserRegistrationRequestBody {
        val userRegistrationRequestBody = UserRegistrationRequestBody()
        userRegistrationRequestBody.userName = userName
        userRegistrationRequestBody.firstName = firstName
        userRegistrationRequestBody.lastName = lastName
        userRegistrationRequestBody.phoneNumber = phoneNumber
        userRegistrationRequestBody.email = email
        return userRegistrationRequestBody
    }

    fun buyOrderRequest(quantity: String, price: String): AddOrderRequestBody {
        val addOrderRequestBody = AddOrderRequestBody()
        addOrderRequestBody.quantity = quantity
        addOrderRequestBody.price = price
        addOrderRequestBody.type = OrderType.BUY.toString()
        return addOrderRequestBody
    }

    fun sellOrderRequest(quantity: String, price: String, esopType: EsopType): AddOrderRequestBody {
        val addOrderRequestBody = AddOrderRequestBody()
        addOrderRequestBody.quantity = quantity
        addOrderRequestBody.price = price
        addOrderRequestBody.type = OrderType.SELL.toString()
        addOrderRequestBody.esopType = esopType.toString()
        return addOrderRequestBody
    }

    fun addWalletMoneyRequestBody(quantity: String): AddWalletMoneyRequestBody {
        val addWalletMoneyRequestBody = AddWalletMoneyRequestBody()
        addWalletMoneyRequestBody.amount = quantity
        return addWalletMoneyRequestBody
    }
}