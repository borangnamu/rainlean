package com.rainlean.data.remote.kma

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KmaUltraShortNcstResponse(
    @SerialName("response")
    val response: KmaResponseWrapper
)

@Serializable
data class KmaResponseWrapper(
    @SerialName("body")
    val body: KmaResponseBody
)

@Serializable
data class KmaResponseBody(
    @SerialName("items")
    val items: KmaItemContainer
)

@Serializable
data class KmaItemContainer(
    @SerialName("item")
    val item: List<KmaNcstItem>
)

@Serializable
data class KmaNcstItem(
    @SerialName("category")
    val category: String,
    @SerialName("obsrValue")
    val value: String
)

