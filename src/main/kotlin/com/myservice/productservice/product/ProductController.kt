package com.myservice.productservice.product

import com.myservice.productservice.product.response.ProductResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/products")
class ProductController(private val productService: ProductService) {

    @GetMapping("/{id}")
    fun getProductById(@PathVariable id:Long): Mono<ProductResponse> {
        return productService.getProductById(id)
    }

    @PutMapping("/{id}")
    fun updateProductById(@RequestBody product: ProductResponse, @PathVariable id: Long, ): Mono<ProductResponse> {
        return productService.updateProductData(product)
    }

}