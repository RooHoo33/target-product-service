package com.myservice.productservice.product.response

import com.fasterxml.jackson.annotation.JsonProperty

data class ProductPriceResponse(
    val value: Double,

    @JsonProperty("currency_code")
    val currency: CurrencyCode
)
