package com.myservice.productservice.product

import com.myservice.productservice.error.exceptions.BadRequestException
import com.myservice.productservice.error.exceptions.NotFoundException
import com.myservice.productservice.product.price.ProductPrice
import com.myservice.productservice.product.price.ProductPriceRepository
import com.myservice.productservice.product.productinfo.ProductInfoResponse
import com.myservice.productservice.product.productinfo.ProductInfoWebClient
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2

@Service
class ProductService(
    private val productPriceRepository: ProductPriceRepository,
    private val productInfoWebClient: ProductInfoWebClient
) {

    private val logger = KotlinLogging.logger {  }

    fun getProductPriceById(productId: Long): Mono<ProductPrice> {
        return productPriceRepository.findById(productId)
            .switchIfEmpty(Mono.error(NotFoundException("Could not find price for product with ID of $productId")))
    }

    fun getProductInfoById(productId: Long): Mono<ProductInfoResponse> {
        return productInfoWebClient.getProduct(productId)
            .onErrorResume(WebClientResponseException::class.java) { error ->
                if (error.statusCode == HttpStatus.NOT_FOUND){
                     Mono.error(NotFoundException("Could not find product with ID of $productId"))
                } else  {
                    Mono.error(BadRequestException("Could not proccess request with product with ID of $productId"))
                }
            }
    }

    fun getProductById(productId: Long): Mono<ProductResponse> {
        return getProductInfoById(productId)
            .zipWhen {
                getProductPriceById(productId)
            }
            .map { marshallProductData(it) }
    }

    fun marshallProductData(tuple2: Tuple2<ProductInfoResponse, ProductPrice>): ProductResponse {
        val info = tuple2.t1
        val price = tuple2.t2
        return ProductResponse(
            info.data.product.productId.toLong(),
            info.data.product.item.productDescription.title,
            ProductPriceResponse(price.price, price.currencyCode)
        )
    }
}