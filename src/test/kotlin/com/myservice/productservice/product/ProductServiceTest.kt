package com.myservice.productservice.product

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.myservice.productservice.error.exceptions.BadRequestException
import com.myservice.productservice.error.exceptions.NotFoundException
import com.myservice.productservice.product.price.ProductPrice
import com.myservice.productservice.product.price.ProductPriceRepository
import com.myservice.productservice.product.productinfo.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductServiceTest {


    @Autowired
    lateinit var productService: ProductService

    @Autowired
    lateinit var productPriceRepository: ProductPriceRepository

    lateinit var mockServer: MockWebServer

    private val productInfoResponse = ProductInfoResponse(
        ProductInfoData(
            ProductInfoProduct(
                "",
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
    fun `test the getProductPriceById function returns the product price`() {

        val setup = productPriceRepository.save(ProductPrice(123456789, 22.40, CurrencyCode.AUD))
        val find = productService.getProductPriceById(123456789)
        val composite = Mono.from(setup)
            .then(find)
        StepVerifier.create(composite)
            .consumeNextWith { product ->
                Assertions.assertAll(
                    { Assertions.assertEquals(123456789, product.productId) },
                    { Assertions.assertEquals(CurrencyCode.AUD, product.currencyCode) },
                )
            }
            .verifyComplete()
    }

    @Test
    fun `test the getProductPriceById function returns an empty mono with a bad product id`() {
        val setup = productPriceRepository.save(ProductPrice(123456789, 22.40, CurrencyCode.AUD))
        val find = productService.getProductPriceById(9999)
        val composite = Mono.from(setup)
            .then(find)
        StepVerifier.create(composite)
            .expectErrorMatches {
                it is NotFoundException
            }
            .verify()
    }

    @Test
    fun `test the getProductInfoById function returns data properly`() {
        mockServer.enqueue(
            MockResponse().setResponseCode(200).setBody(jacksonObjectMapper().writeValueAsString(productInfoResponse))
                .addHeader("Content-Type", "application/json")
        )

        val mono = productService.getProductInfoById(1234)
        StepVerifier.create(mono)
            .consumeNextWith { productInfo ->
                Assertions.assertAll(
                    { Assertions.assertEquals(productInfoResponse, productInfo) },
                )
            }
            .verifyComplete()
    }

    @Test
    fun `test the getProductInfoById function throws an not found error when product id doesnt exist`() {
        mockServer.enqueue(MockResponse().setResponseCode(404))

        val notFoundMono = productService.getProductInfoById(1234)
        StepVerifier.create(notFoundMono)
            .expectErrorMatches {
                it is NotFoundException
            }
            .verify()
    }

    @Test
    fun `test the getProductInfoById function throws a bad request error when the api returns a bad request`(){

        mockServer.enqueue(MockResponse().setResponseCode(400))

        val badRequestMono = productService.getProductInfoById(1234)
        StepVerifier.create(badRequestMono)
            .expectErrorMatches {
                it is BadRequestException
            }
            .verify()
    }
}