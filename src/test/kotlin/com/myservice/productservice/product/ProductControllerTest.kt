package com.myservice.productservice.product

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.myservice.productservice.error.ErrorMessage
import com.myservice.productservice.product.price.ProductPrice
import com.myservice.productservice.product.price.ProductPriceRepository
import com.myservice.productservice.product.productinfo.*
import com.myservice.productservice.product.response.CurrencyCode
import com.myservice.productservice.product.response.ProductPriceResponse
import com.myservice.productservice.product.response.ProductResponse
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.runner.RunWith
import org.mockito.BDDMockito
import org.mockito.BDDMockito.any
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductControllerTest {
    @Autowired
    lateinit var webClient: WebTestClient

    @MockBean
    lateinit var productPriceRepository: ProductPriceRepository


    lateinit var mockServer: MockWebServer

    private val productInfoResponse = ProductInfoResponse(
        ProductInfoData(
            ProductInfoProduct(
                "12345",
                ProductInfoItem(
                    ProductDescription("super movie", "something goes here"),
                    ProductEnrichment(ProductImages("https://www.image.com/picture")),
                    ProductClassification("dvd", "movies"),
                    ProductBrand("Super Movies Inc")
                )
            )
        )
    )

    @BeforeAll
    fun beforeAll() {
        mockServer = MockWebServer()
        mockServer.start(43632)
    }

    @AfterAll
    fun afterTests() {
        mockServer.shutdown()
    }


    @Test
    fun `test the get product endpoint returns product with correct id`() {
        mockServer.enqueue(
            MockResponse().setResponseCode(200).setBody(jacksonObjectMapper().writeValueAsString(productInfoResponse))
                .addHeader("Content-Type", "application/json")
        )
        val productPrice = ProductPrice(12345, 22.50, CurrencyCode.USD)
        val productPriceMono = Mono.just(productPrice)
        given(productPriceRepository.findById(12345))
            .willReturn(productPriceMono)

        this.webClient.get().uri("/products/12345")
            .exchange()
            .expectStatus().isOk
            .expectBody(ProductResponse::class.java)
            .isEqualTo(
                ProductResponse(
                    12345,
                    productInfoResponse.data.product.item.productDescription.title,
                    ProductPriceResponse(22.50, CurrencyCode.USD)
                )
            )
    }

    @Test
    fun `test the get product endpoint returns 404 error message when the id is missing in the api`() {
        mockServer.enqueue(MockResponse().setResponseCode(404))
        val productPrice = ProductPrice(12345, 22.50, CurrencyCode.USD)
        val productPriceMono = Mono.just(productPrice)
        given(productPriceRepository.findById(12345))
            .willReturn(productPriceMono)
        this.webClient.get().uri("/products/12345")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(ErrorMessage::class.java)
            .consumeWith {
                Assertions.assertAll(
                    { assertNotNull(it.responseBody) },
                    { assertEquals(it.responseBody!!.message, "Could not find product with ID of 12345") }
                )
            }

    }

    @Test
    fun `test get products endpoint returns 404 error message when the id is missing in the api and price database`() {
        mockServer.enqueue(MockResponse().setResponseCode(404))
        given(productPriceRepository.findById(12345))
            .willReturn(Mono.empty())
        this.webClient.get().uri("/products/12345")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(ErrorMessage::class.java)
            .consumeWith {
                Assertions.assertAll(
                    { assertNotNull(it.responseBody) },
                    { assertEquals(it.responseBody!!.message, "Could not find product with ID of 12345") }
                )
            }

    }

    @Test
    fun `test get product endpoint return 404 error message when the id is missing in the price database`() {
        mockServer.enqueue(
            MockResponse().setResponseCode(200).setBody(jacksonObjectMapper().writeValueAsString(productInfoResponse))
                .addHeader("Content-Type", "application/json")
        )
        given(productPriceRepository.findById(12345))
            .willReturn(Mono.empty())
        this.webClient.get().uri("/products/12345")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(ErrorMessage::class.java)
            .consumeWith {
                Assertions.assertAll(
                    { assertNotNull(it.responseBody) },
                    { assertEquals("Could not find price for product with ID of 12345", it.responseBody!!.message) }
                )
            }
    }

    @Test
    fun `test the update product endpoint with incorrect id`() {

        val product = ProductResponse(
            12345,
            "Cool Movie",
            ProductPriceResponse(10.25, CurrencyCode.EUR)
        )

        given(productPriceRepository.findById(12345))
            .willReturn(Mono.empty())
        this.webClient.put().uri("/products/12345")
            .bodyValue(product)
            .exchange()
            .expectStatus().isNotFound
            .expectBody(ErrorMessage::class.java)
            .consumeWith {
                Assertions.assertAll(
                    { assertNotNull(it.responseBody) },
                    { assertEquals("Could not find price for product with ID of 12345", it.responseBody!!.message) }
                )
            }
    }


    @Test
    fun `tests the update product endpoint updates the product price when called with existing id`() {
        val currentProductPrice = ProductPrice(12345, 22.50, CurrencyCode.USD)
        val newProductPrice = ProductPrice(12345, 9.99, CurrencyCode.USD)

        val product = ProductResponse(
            12345,
            "Cool Movie",
            ProductPriceResponse(newProductPrice.price, newProductPrice.currencyCode)
        )

        given(productPriceRepository.findById(12345))
            .willReturn(Mono.just(currentProductPrice))
        given(productPriceRepository.save(any()))
            .willReturn(Mono.just(newProductPrice))

        this.webClient.put().uri("/products/12345")
            .bodyValue(product)
            .exchange()
            .expectStatus().isOk
            .expectBody(ProductResponse::class.java)
            .consumeWith {
                Assertions.assertAll(
                    { assertNotNull(it.responseBody) },
                    { assertEquals(product, it.responseBody) },
                    {BDDMockito.verify(productPriceRepository).save(newProductPrice)}
                )
            }
    }
}