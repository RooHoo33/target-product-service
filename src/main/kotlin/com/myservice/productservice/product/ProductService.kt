package com.myservice.productservice.product

import com.myservice.productservice.error.exceptions.BadRequestException
import com.myservice.productservice.error.exceptions.NotFoundException
import com.myservice.productservice.product.price.ProductPrice
import com.myservice.productservice.product.price.ProductPriceRepository
import com.myservice.productservice.product.productinfo.ProductInfoResponse
import com.myservice.productservice.product.productinfo.ProductInfoWebClient
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2

@Service
class ProductService(
    private val productPriceRepository: ProductPriceRepository,
    private val productInfoWebClient: ProductInfoWebClient
) {

    fun getProductPriceById(productId: Long): Mono<ProductPrice> {
        return productPriceRepository.findById(productId)
            .switchIfEmpty(Mono.error(NotFoundException("Could not find price for product with ID of $productId")))
    }

    fun getProductInfoById(productId: Long): Mono<ProductInfoResponse> {
        return productInfoWebClient.getProduct(productId)
            .onErrorResume(WebClientResponseException::class.java) { error ->
                if (error.statusCode == HttpStatus.NOT_FOUND) {
                    Mono.error(NotFoundException("Could not find product with ID of $productId"))
                } else {
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
            marshProductPriceResponse(price)
        )
    }

    fun marshProductPriceResponse(productPrice: ProductPrice): ProductPriceResponse {
       return ProductPriceResponse(productPrice.price, productPrice.currencyCode)
    }

    fun updateProductData(product: ProductResponse): Mono<ProductResponse> {
        return getProductPriceById(product.id)
            .flatMap {

                if (it.price != product.currentPrice.value || it.currencyCode != product.currentPrice.currenyCode) {
                    productPriceRepository.save(
                        ProductPrice(
                            it.productId,
                            product.currentPrice.value,
                            product.currentPrice.currenyCode
                        )
                    ).map { updatedProductPrice ->
                        product.currentPrice = marshProductPriceResponse(updatedProductPrice)
                        product
                    }
                } else {
                    Mono.just(product)
                }
            }
    }
}