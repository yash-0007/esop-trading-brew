package com.esops.validation.configuration

import java.util.*
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass


@Target(AnnotationTarget.FIELD)
@MustBeDocumented
@Constraint(validatedBy = [BreakupArrayValidator::class])
annotation class BreakupArrayValid(
    val message: String = "{javax.validation.constraints.BreakupArrayValid.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class BreakupArrayValidator : ConstraintValidator<BreakupArrayValid, Array<Double>> {
    override fun isValid(breakupCycle: Array<Double>?, context: ConstraintValidatorContext): Boolean {
        context.disableDefaultConstraintViolation()
        if(breakupCycle == null) {
            context.buildConstraintViolationWithTemplate("Could not parse breakup").addConstraintViolation()
            return false
        }
        var sum = 0.0
        for(cycle in breakupCycle) {
            if(cycle < 0) {
                context.buildConstraintViolationWithTemplate("Breakup cannot be negative").addConstraintViolation()
                return false
            }
            sum += cycle
        }
        if(sum != 1.0) {
            context.buildConstraintViolationWithTemplate("Breakup sum should be one").addConstraintViolation()
            return false
        }
        return true
    }

}