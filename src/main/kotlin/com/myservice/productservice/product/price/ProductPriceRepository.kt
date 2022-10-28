package com.myservice.productservice.product.price

import org.springframework.data.repository.reactive.ReactiveCrudRepository


interface ProductPriceRepository : ReactiveCrudRepository<ProductPrice, Long>
