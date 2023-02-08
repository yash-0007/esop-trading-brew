package com.esops.validation.configuration

import com.esops.configuration.InventoryLimitConfiguration
import java.lang.NumberFormatException
import java.math.BigInteger
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@MustBeDocumented
@Constraint(validatedBy = [MinMaxValueValidatorInventoryLimit::class])
annotation class ValidMinMaxValueInventoryLimit(
    val message: String = "{javax.validation.constraints.ValidMinMaxValue.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class MinMaxValueValidatorInventoryLimit : ConstraintValidator<ValidMinMaxValueInventoryLimit, InventoryLimitConfiguration> {

    private val minLimit = BigInteger.ZERO
    private val maxLimit = BigInteger("100000000000000000000")
    override fun isValid(value: InventoryLimitConfiguration, context: ConstraintValidatorContext): Boolean {
        context.disableDefaultConstraintViolation()
        try {
            val minValue = BigInteger(value.min)
            val maxValue = BigInteger(value.max)
            if (minValue in minLimit..maxLimit && maxValue in minLimit..maxLimit && minValue < maxValue) {
                return true
            }
            if (minValue !in minLimit..maxLimit) {
                context.buildConstraintViolationWithTemplate("Limit of min field is 0 to 10^20")
                    .addConstraintViolation()
            }
            if (maxValue !in minLimit..maxLimit) {
                context.buildConstraintViolationWithTemplate("Limit of max field is 0 to 10^20")
                    .addConstraintViolation()
            }
            if (minValue > maxValue) {
                context.buildConstraintViolationWithTemplate("min field must be less than max field")
                    .addConstraintViolation()
            }
            return false
        } catch (e: NumberFormatException) {
            context.buildConstraintViolationWithTemplate("Limits are not a valid number").addConstraintViolation()
            return false
        }
    }
}
