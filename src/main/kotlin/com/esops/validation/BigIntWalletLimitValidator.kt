package com.esops.validation

import com.esops.configuration.InventoryLimitConfiguration
import com.esops.configuration.WalletLimitConfiguration
import jakarta.inject.Inject
import java.lang.Exception
import java.lang.NumberFormatException
import java.math.BigInteger
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass


@Target(AnnotationTarget.FIELD)
@MustBeDocumented
@Constraint(validatedBy = [BigIntWalletLimitValidator::class])
annotation class ValidBigIntWalletLimit(
    val message: String = "{javax.validation.constraints.NotBlank.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

@Target(AnnotationTarget.FIELD)
@MustBeDocumented
@Constraint(validatedBy = [BigIntInventoryLimitValidator::class])
annotation class ValidBigIntInventoryLimit(
    val message: String = "{javax.validation.constraints.NotBlank.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class BigIntWalletLimitValidator : ConstraintValidator<ValidBigIntWalletLimit, String> {
    @Inject
    lateinit var walletLimitConfiguration: WalletLimitConfiguration
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        context.disableDefaultConstraintViolation()
        try {
            if(value == null) {
                return true
            }
            val bigIntValue = BigInteger(value)
            if (bigIntValue in BigInteger(walletLimitConfiguration.min!!)..BigInteger(walletLimitConfiguration.max!!)) {
                return true
            }
            context.buildConstraintViolationWithTemplate("value not in wallet limits of the system").addConstraintViolation()
            return false
        } catch (e: NumberFormatException) {
            context.buildConstraintViolationWithTemplate("wallet limits are not valid numbers").addConstraintViolation()
            return false
        }
        catch(e: Exception){
            context.buildConstraintViolationWithTemplate(e.message).addConstraintViolation()
            return false
        }
    }
}
class BigIntInventoryLimitValidator : ConstraintValidator<ValidBigIntInventoryLimit, String> {

    @Inject
    lateinit var inventoryLimitConfiguration: InventoryLimitConfiguration
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        context.disableDefaultConstraintViolation()
        try {
            if(value == null) {
                return true
            }
            val bigIntValue = BigInteger(value)
            if (bigIntValue in BigInteger(inventoryLimitConfiguration.min!!)..BigInteger(inventoryLimitConfiguration.max!!)) {
                return true
            }
            context.buildConstraintViolationWithTemplate("value not in inventory limits of the system").addConstraintViolation()
            return false
        } catch (e: NumberFormatException) {
            context.buildConstraintViolationWithTemplate("inventory limits are not a valid number").addConstraintViolation()
            return false
        }
    }
}
