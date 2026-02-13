package com.chumakov123.casedocket.domain.model.imaging

data class DocumentLayout(
    val headerRegion: ImageRegion,
    val tableCells: List<List<ImageRegion>>
)