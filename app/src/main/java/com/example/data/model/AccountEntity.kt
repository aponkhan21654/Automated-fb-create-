package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_log")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firstName: String,
    val lastName: String,
    val emailOrPhone: String,
    val password: String,
    val birthDay: Int,
    val birthMonth: Int,
    val birthYear: Int,
    val gender: String,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "Registered",
    val notes: String = "",
    val cookie: String = "",
    val uid: String = ""
)
