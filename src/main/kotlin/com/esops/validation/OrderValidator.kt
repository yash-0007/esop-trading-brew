package com.esops.validation

import com.esops.entity.EsopType
import com.esops.model.AddOrderRequestBody
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [OrderValidator::class])
annotation class ValidOrder(
    val message: String = "{com.esops.validation.ValidOrder.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class OrderValidator : ConstraintValidator<ValidOrder, AddOrderRequestBody> {
    override fun isValid(addOrderRequestBody: AddOrderRequestBody?, context: ConstraintValidatorContext?): Boolean {
        context!!.disableDefaultConstraintViolation()
        if (addOrderRequestBody!!.type == "SELL") {
            val validateEsopType = validateESOPTypeDuringSell(addOrderRequestBody.esopType)
            if (validateEsopType) return true
            context.buildConstraintViolationWithTemplate("sell order needs to have esopType with value ${EsopType.PERFORMANCE} or ${EsopType.NON_PERFORMANCE}")
                .addConstraintViolation()
            return false
        }
        if (addOrderRequestBody.type == "BUY") {
            val validateEsopType = validateESOPTypeDuringBuy(addOrderRequestBody.esopType)
            if (validateEsopType) return true
            context.buildConstraintViolationWithTemplate("buy order need not have esopType field")
                .addConstraintViolation()
            return false
        }
        return true
    }

    private fun validateESOPTypeDuringSell(esopType: String?): Boolean {
        return esopType!= null && (esopType == EsopType.PERFORMANCE.toString() || esopType == EsopType.NON_PERFORMANCE.toString())
    }

    private fun validateESOPTypeDuringBuy(esopType: String?): Boolean {
        return esopType == null
    }

}
