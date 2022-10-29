package com.myservice.productservice.product.productinfo

import com.fasterxml.jackson.annotation.JsonProperty

data class ProductInfoResponse(
    val data: ProductInfoData
)

data class ProductInfoData(
    val product: ProductInfoProduct
)
data class ProductInfoProduct(
    @JsonProperty("tcin")
    val productId: String,
    val item: ProductInfoItem
)

data class ProductInfoItem(
    @JsonProperty("product_description")
    val productDescription: ProductDescription,

    @JsonProperty("enrichment")
    val productEnrichment: ProductEnrichment,

    @JsonProperty("product_classification")
    val productClassification: ProductClassification,

    @JsonProperty("primary_brand")
    val productPrimaryBrand: ProductBrand
)

data class ProductDescription(
    val title: String,

    @JsonProperty("downstream_description")
    val downstreamDescription: String
)

data class ProductEnrichment(
    val images: ProductImages
)

data class ProductImages(
    @JsonProperty("primary_image_url")
    val primaryImageUrl: String
)

data class ProductClassification(
    @JsonProperty("product_type_name")
    val productTypeName: String,

    @JsonProperty("merchandise_type_name")
    val merchandiseTypeName: String,
)

data class ProductBrand(
    val name: String
)