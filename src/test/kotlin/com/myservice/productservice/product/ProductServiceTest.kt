package com.myservice.productservice.product

import com.myservice.productservice.product.price.ProductPrice
import com.myservice.productservice.product.price.ProductPriceRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@SpringBootTest
class ProductServiceTest {


    @Autowired
    lateinit var productService: ProductService

    @Autowired
    lateinit var productPriceRepository: ProductPriceRepository

    @Test
    fun `test the getProductPriceById function returns the product price`() {

        val setup = productPriceRepository.save(ProductPrice(123456789, 22.40, CurrencyCode.AUD))
        val find = productService.getProductPriceById(123456789)
        val composite = Mono.from(setup)
            .then(find)
        StepVerifier.create(composite)
            .consumeNextWith { prod ->
                Assertions.assertAll(
                    { Assertions.assertEquals(123456789, prod.productId) },
                    { Assertions.assertEquals(CurrencyCode.USD, prod.currencyCode) },
                )
            }
            .verifyComplete()
    }

    @Test
    fun `test the getProductPriceById function returns an empty mono with a bad product id`(){
        val setup = productPriceRepository.save(ProductPrice(123456789, 22.40, CurrencyCode.AUD))
        val find = productService.getProductPriceById(9999)
        val composite = Mono.from(setup)
            .then(find)
        StepVerifier.create(composite)
            .expectNextCount(0)
            .verifyComplete()
    }
}