package com.example.bilibili.data.model

data class RegisterRequest(
    val email: String,
    val nickName: String,
    val registerPassword: String,
    val reRegisterPassword: String,
    val checkCode: String,
    val checkCodeKey: String
)