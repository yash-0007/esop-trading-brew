package com.esops.controller

import com.esops.exception.InventoryLimitExceededException
import com.esops.exception.UserNotFoundException
import com.esops.exception.UserNotUniqueException
import com.esops.exception.WalletLimitExceededException
import com.esops.model.ErrorResponse
import com.fasterxml.jackson.core.JsonParseException
import io.micronaut.core.bind.exceptions.UnsatisfiedArgumentException
import io.micronaut.core.convert.exceptions.ConversionErrorException
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException
import javax.validation.ConstraintViolationException

@Controller
class GlobalExceptionController {

    @Error(global = true)
    fun jsonError(error: JsonParseException): HttpResponse<ErrorResponse> {
        return HttpResponse.badRequest(ErrorResponse(listOf("could not parse json")))
    }

    @Error(global = true)
    fun constraintError(error: ConstraintViolationException): HttpResponse<ErrorResponse> {
        return HttpResponse.badRequest(ErrorResponse(error.constraintViolations.toList().map { it.message }))
    }

    @Error(global = true)
    fun conversionError(error: ConversionErrorException): HttpResponse<ErrorResponse> {
        return HttpResponse.badRequest(ErrorResponse(listOf(error.conversionError.cause.message)))
    }

    @Error(global = true)
    fun unsatisfiedArgumentError(error: UnsatisfiedArgumentException): HttpResponse<ErrorResponse> {
        return HttpResponse.badRequest(ErrorResponse(listOf(error.message)))
    }

    @Error(global = true)
    fun unsatisfiedRouteError(error: UnsatisfiedRouteException): HttpResponse<ErrorResponse> {
        return HttpResponse.badRequest(ErrorResponse(listOf(error.message)))
    }

    @Error(status = HttpStatus.NOT_FOUND, global = true)
    fun invalidEndpointError(): ErrorResponse {
        return ErrorResponse(listOf("not a valid endpoint"))
    }

    @Error(global = true)
    fun userNotFoundError(error: UserNotFoundException): HttpResponse<ErrorResponse> {
        return HttpResponse.notFound(ErrorResponse(error.errorList))
    }

    @Error(global = true)
    fun userNotUniqueError(error: UserNotUniqueException): HttpResponse<ErrorResponse> {
        return HttpResponse.badRequest(ErrorResponse(error.errorList))
    }

    @Error(global = true)
    fun walletLimitExceededError(error: WalletLimitExceededException): HttpResponse<ErrorResponse> {
        return HttpResponse.badRequest(ErrorResponse(error.errorList))
    }

    @Error(global = true)
    fun inventoryLimitExceededError(error: InventoryLimitExceededException): HttpResponse<ErrorResponse> {
        return HttpResponse.badRequest(ErrorResponse(error.errorList))
    }

    @Error(global = true)
    fun error(e: Throwable): ErrorResponse {
        return ErrorResponse(listOf(e.message))
    }
}
