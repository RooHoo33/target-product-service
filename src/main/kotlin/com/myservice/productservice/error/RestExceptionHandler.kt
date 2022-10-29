package com.myservice.productservice.error

import com.myservice.productservice.error.exceptions.BadRequestException
import com.myservice.productservice.error.exceptions.NotFoundException
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class RestExceptionHandler {

    private val logger = KotlinLogging.logger {  }

    @ExceptionHandler(NotFoundException::class)
    fun notFound(exception: NotFoundException): ResponseEntity<ErrorMessage> {
        logException(exception)
        return ResponseEntity(ErrorMessage(message = exception.message ?: NOT_FOUND_MESSAGE_DEFAULT), HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(BadRequestException::class)
    fun badRequest(exception: BadRequestException): ResponseEntity<ErrorMessage> {
        logException(exception)
        return ResponseEntity(ErrorMessage(message = exception.message ?: NOT_FOUND_MESSAGE_DEFAULT), HttpStatus.BAD_REQUEST)
    }

    private fun logException(exception:Exception){
        logger.warn { exception }
        logger.warn { exception.stackTraceToString() }
    }

    companion object{
        const val NOT_FOUND_MESSAGE_DEFAULT = "Item could not be found."
    }


}