package com.dinhtc.taskmaster.service

import com.dinhtc.taskmaster.model.request.AddMaterialRequest
import com.dinhtc.taskmaster.model.request.AddTaskRequest
import com.dinhtc.taskmaster.model.request.CollectPointRequest
import com.dinhtc.taskmaster.model.request.DeleteMaterialRequest
import com.dinhtc.taskmaster.model.request.DeleteMediaRequest
import com.dinhtc.taskmaster.model.request.LoginRequest
import com.dinhtc.taskmaster.model.response.*
import com.dinhtc.taskmaster.utils.ApiResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {
    @POST("auth/login")
    suspend fun loginUser(@Body loginRequest : LoginRequest): ApiResponse<Any>
    @POST("firebase/save")
    suspend fun saveFirebaseToken(@Query("firebaseToken") firebaseToken: String): ApiResponse<Any>
    @GET("mobile/job_type/list")
    suspend fun getListJobType(): ApiResponse<ListJobTypeResponse>
    @GET("mobile/employees/{id}")
    suspend fun getListEmployeeNotById(@Path("id") id : Int): ApiResponse<ListEmployeeResponse>

    @GET("mobile/employees")
    suspend fun getListEmployee(): ApiResponse<ListEmployeeResponse>

    @GET("mobile/collect_point")
    suspend fun getListCollectPoint(): ApiResponse<ListCollectPointResponse>

    @Multipart
    @POST("mobile/upload_image_video")
    suspend fun uploadMultiImageVideo(
        @Part("job_id") jobId: Int,
        @Part url_image: List<MultipartBody.Part>?,
        @Part url_video: MultipartBody.Part?,
        @Part("media_type") mediaType: Int
    ): ApiResponse<Any>

    @Multipart
    @POST("mobile/upload_image")
    suspend fun uploadMultiImage(
        @Part("job_id") jobId: Int,
        @Part url_image: List<MultipartBody.Part>?,
        @Part("media_type") mediaType: Int
    ): ApiResponse<Any>

    @Multipart
    @POST("mobile/upload_video")
    suspend fun uploadVideo(
        @Part("job_id") jobId: Int,
        @Part url_video: MultipartBody.Part?,
        @Part("media_type") mediaType: Int
    ): ApiResponse<Any>

    @POST("mobile/add_collect_point")
    suspend fun addCollectPoint(@Body collectPoint: CollectPointRequest): ApiResponse<Any>

    @POST("mobile/add_job")
    suspend fun addTask(@Body collectPoint: AddTaskRequest): ApiResponse<Any>

    @GET("mobile/material_list")
    suspend fun getListMaterial(): ApiResponse<ListMaterialResponse>

    @POST("mobile/add_material")
    suspend fun addMaterial(@Body collectPoint: AddMaterialRequest): ApiResponse<Any>

    @GET("mobile/details/{id}")
    suspend fun getJobDetails(@Path("id") id : Int): ApiResponse<JobDetailsResponse>

    @PUT("mobile/update/update_state_job/{jobId}/{newStateId}")
    suspend fun updateStateJob(
        @Path("jobId") jobId: Int,
        @Path("newStateId") newStateId: Int
    ): ApiResponse<UpdateJobsResponse>

    @POST("mobile/delete_media")
    suspend fun deleteMedia(@Body collectPoint: DeleteMediaRequest): ApiResponse<Any>

    @POST("mobile/delete_material")
    suspend fun deleteMaterial(@Body deleteMaterial: DeleteMaterialRequest): ApiResponse<Any>


}