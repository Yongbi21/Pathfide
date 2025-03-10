package com.example.pathfide.network

import com.example.pathfide.Model.UploadResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("upload_payment_proof.php")
    fun uploadProof(
        @Part file: MultipartBody.Part
    ): Call<UploadResponse>
}