package com.chumakov123.casedocket.domain.model.imaging

data class DocumentLayout(
    val headerImage: ByteArray,
    val tableImage: ByteArray,
    val tableCells: List<List<ImageRegion>>
)