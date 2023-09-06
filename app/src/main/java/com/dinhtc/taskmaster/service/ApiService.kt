package com.dinhtc.taskmaster.service

import com.dinhtc.taskmaster.model.request.AddMaterialRequest
import com.dinhtc.taskmaster.model.request.AddTaskRequest
import com.dinhtc.taskmaster.model.request.CollectPointRequest
import com.dinhtc.taskmaster.model.request.DataUpdateJobRequest
import com.dinhtc.taskmaster.model.request.DeleteMaterialRequest
import com.dinhtc.taskmaster.model.request.DeleteMediaRequest
import com.dinhtc.taskmaster.model.request.LoginRequest
import com.dinhtc.taskmaster.model.request.UpdateStateRequest
import com.dinhtc.taskmaster.model.response.*
import com.dinhtc.taskmaster.utils.ApiResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {
    @POST("auth/login")
    suspend fun loginUser(@Body loginRequest : LoginRequest): ApiResponse<Any>
    @POST("auth/logout")
    suspend fun loginOut(
        @Header("Authorization") token: String
    ): ApiResponse<Any>
    @POST("firebase/save")
    suspend fun saveFirebaseToken(
        @Header("Authorization") token: String,
        @Query("firebaseToken") firebaseToken: String,
        @Query("deviceId") deviceId: String?,
        @Query("deviceName") deviceName: String?
    ): ApiResponse<Any>
    @GET("mobile/job_type/list")
    suspend fun getListJobType(): ApiResponse<ListJobTypeResponse>
    @GET("mobile/employees/{id}")
    suspend fun getListEmployeeNotById(@Path("id") id : Int): ApiResponse<ListEmployeeResponse>
    @GET("mobile/employees/jobId/{jobId}")
    suspend fun getListEmployeeByJobId(@Path("jobId") jobId : Int): ApiResponse<ListEmployeeResponse>

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

    @GET("mobile/details/{jobId}/{empId}")
    suspend fun getJobDetails(
        @Path("jobId") jobId : Int,
        @Path("empId") empId : Int
    ): ApiResponse<JobDetailsResponse>

    @PUT("mobile/update/update_state_job")
    suspend fun updateStateJob(@Body dataUpdate: UpdateStateRequest): ApiResponse<UpdateJobsResponse>

    @POST("mobile/delete_media")
    suspend fun deleteMedia(@Body collectPoint: DeleteMediaRequest): ApiResponse<Any>

    @POST("mobile/delete_material")
    suspend fun deleteMaterial(@Body deleteMaterial: DeleteMaterialRequest): ApiResponse<Any>

    @GET("mobile/userprofile/{username}")
    suspend fun getUserProfile(@Path("username") username : String): ApiResponse<Any>

    @POST("mobile/update_job_detail")
    suspend fun updateJobDetails(@Body dataUpdate: DataUpdateJobRequest): ApiResponse<Any>

    @GET("mobile/search")
    suspend fun search(
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?,
        @Query("empStatus") empStatus: Int?,
        @Query("empId") empId: Int?,
        @Query("status") status: Int?,
        @Query("paymentStatus") paymentStatus: Int?,
        @Query("jobId") jobId: Int?,
        @Query("collectPoint") collectPoint: String?
    ): ApiResponse<ListJobSearchResponse>


}