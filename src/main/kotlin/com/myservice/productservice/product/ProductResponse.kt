package com.myservice.productservice.product

import com.fasterxml.jackson.annotation.JsonProperty

data class ProductResponse(
    val id: Long,
    val name: String,

    @JsonProperty("current_price")
    var currentPrice: ProductPriceResponse
)
