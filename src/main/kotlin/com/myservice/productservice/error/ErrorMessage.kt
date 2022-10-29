package com.myservice.productservice.error

import java.time.LocalDateTime
import java.util.UUID

data class ErrorMessage(
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val id: UUID = UUID.randomUUID()
)
