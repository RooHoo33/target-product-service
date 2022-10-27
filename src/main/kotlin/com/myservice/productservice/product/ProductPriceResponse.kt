package com.myservice.productservice.product

import com.fasterxml.jackson.annotation.JsonProperty

data class ProductPriceResponse(
    val value: Double,

    @JsonProperty("currency_code")
    val currenyCode: CurrencyCode
)
