package com.kavikiran.vidyarthibus.model

data class Route(
    val id: String = "",
    val name: String = "",
    val stops: List<String> = emptyList()
)