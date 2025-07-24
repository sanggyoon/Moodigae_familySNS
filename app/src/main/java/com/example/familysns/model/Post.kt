package com.example.familysns.model

import com.google.firebase.Timestamp

data class Post(
    val authorId: String = "",
    val message: String = "",
    val imageUrls: List<String> = emptyList(),
    val participants: List<String> = emptyList(),
    val tag: String? = null,
    val createdAt: Timestamp? = null
)