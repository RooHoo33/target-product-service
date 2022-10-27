package com.myservice.productservice.product

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/products")
class ProductController {

    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: Long):ProductResponse{
        return ProductResponse(
            id,
            "The Big Lebowski (Blu-ray) (Widescreen)",
            ProductPriceResponse(13.49, CurrencyCode.USD)
        )
    }

}