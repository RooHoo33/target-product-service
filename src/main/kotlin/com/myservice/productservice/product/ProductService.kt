package com.myservice.productservice.product

import com.myservice.productservice.error.exceptions.BadRequestException
import com.myservice.productservice.error.exceptions.NotFoundException
import com.myservice.productservice.product.price.ProductPrice
import com.myservice.productservice.product.price.ProductPriceRepository
import com.myservice.productservice.product.productinfo.ProductInfoResponse
import com.myservice.productservice.product.productinfo.ProductInfoWebClient
import com.myservice.productservice.product.response.ProductPriceResponse
import com.myservice.productservice.product.response.ProductResponse
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

    /**
     * Find the product price in the product price repository. If the product price cannot be found, a
     * NotFoundException is thrown
     */
    fun getProductPriceById(productId: Long): Mono<ProductPrice> {
        return productPriceRepository.findById(productId)
            .switchIfEmpty(Mono.error(NotFoundException("Could not find price for product with ID of $productId")))
    }

    /**
     * Gets the product info by id by calling the product info api. Throws a not found error is the info cannot be
     * found in the api or a bad request if exception if a different problem is encountered when executing the request
     */
    fun getProductInfoById(productId: Long): Mono<ProductInfoResponse> {
        return productInfoWebClient.getProduct(productId)
            .onErrorResume(WebClientResponseException::class.java) { error ->
                if (error.statusCode == HttpStatus.NOT_FOUND) {
                    Mono.error(NotFoundException("Could not find product with ID of $productId"))
                } else {
                    Mono.error(BadRequestException("Could not process request with product with ID of $productId"))
                }
            }
    }

    /**
     * Gets the product price from the product price repository and the product info from the product api. Then merges
     * the data into the product response
     */
    fun getProductById(productId: Long): Mono<ProductResponse> {
        return getProductInfoById(productId)
            .zipWhen {
                getProductPriceById(productId)
            }
            .map { marshallProductData(it) }
    }

    /**
     * Marshalls the product info mono and product price mono into a product object
     */
    fun marshallProductData(tuple2: Tuple2<ProductInfoResponse, ProductPrice>): ProductResponse {
        val info = tuple2.t1
        val price = tuple2.t2
        return ProductResponse(
            info.data.product.productId.toLong(),
            info.data.product.item.productDescription.title,
            marshProductPriceResponse(price)
        )
    }

    /**
     * Used to convert a product price object to the project price response object to be returned by this service
     */
    fun marshProductPriceResponse(productPrice: ProductPrice): ProductPriceResponse {
       return ProductPriceResponse(productPrice.price, productPrice.currencyCode)
    }

    /**
     * Updates product price from the product is the price or currency changes
     */
    fun updateProductData(product: ProductResponse): Mono<ProductResponse> {
        return getProductPriceById(product.id)
            .flatMap {

                if (it.price != product.currentPrice.value || it.currencyCode != product.currentPrice.currency) {
                    productPriceRepository.save(
                        ProductPrice(
                            it.productId,
                            product.currentPrice.value,
                            product.currentPrice.currency
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