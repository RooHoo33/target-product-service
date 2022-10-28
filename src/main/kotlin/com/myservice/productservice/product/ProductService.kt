package com.myservice.productservice.product

import com.myservice.productservice.product.price.ProductPrice
import com.myservice.productservice.product.price.ProductPriceRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ProductService(private val productPriceRepository: ProductPriceRepository) {

    fun getProductPriceById(productId: Long): Mono<ProductPrice> {
        return productPriceRepository.findById(productId)
    }
}