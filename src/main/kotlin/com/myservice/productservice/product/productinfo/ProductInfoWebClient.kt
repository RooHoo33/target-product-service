package com.myservice.productservice.product.productinfo

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class ProductInfoWebClient(
    builder: WebClient.Builder,
    @Value("\${product-info.api.key}")
    val productInfoAPIKey: String,

    @Value("\${product-info.api.url}")
    val productInfoAPIUrl: String


) {

    private final val webClient: WebClient
    private val logger = KotlinLogging.logger {  }

    init {
        println(productInfoAPIKey)
        webClient =
            builder.baseUrl(productInfoAPIUrl)
                .build()
    }

    fun getProduct(productId: Long): Mono<ProductInfoResponse> {
        logger.error { productInfoAPIUrl }
        return this.webClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .queryParam("key", productInfoAPIKey)
                    .queryParam("tcin", productId)
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(ProductInfoResponse::class.java)
    }
}