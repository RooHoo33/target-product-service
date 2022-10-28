package com.myservice.productservice.product

import com.myservice.productservice.product.price.ProductPrice
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/products")
class ProductController(private val productService: ProductService) {

    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: Long): Mono<ProductPrice> {
        return productService.getProductPriceById(id)
            .switchIfEmpty(Mono.error(
                ResponseStatusException(HttpStatus.BAD_REQUEST)
))
    }

}