package com.example.owoo.network

import com.example.owoo.data.LoginRequest
import com.example.owoo.data.LoginResponse
import com.example.owoo.data.ValidationRequest
import com.example.owoo.data.ValidationResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("hisense/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("hisense/validate-cookie")
    suspend fun validateCookie(@Body request: ValidationRequest): Response<ValidationResponse>
}

object RetrofitInstance {
    private const val BASE_URL = "https://owo-api-production.up.railway.app/"

    val api: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }
}
