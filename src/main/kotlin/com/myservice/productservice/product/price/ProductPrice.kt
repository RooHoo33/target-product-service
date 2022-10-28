package com.myservice.productservice.product.price

import com.myservice.productservice.product.CurrencyCode
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("product-prices")
data class ProductPrice(
    @Id
    val productId: Long? = null,

    val price: Double,
    val currencyCode: CurrencyCode
)
