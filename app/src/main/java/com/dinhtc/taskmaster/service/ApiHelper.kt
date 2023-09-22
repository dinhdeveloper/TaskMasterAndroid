package com.dinhtc.taskmaster.service

import com.dinhtc.taskmaster.model.request.AddMaterialRequest
import com.dinhtc.taskmaster.model.request.AddTaskRequest
import com.dinhtc.taskmaster.model.request.CollectPointRequest
import com.dinhtc.taskmaster.model.request.CompactedAndDoneRequest
import com.dinhtc.taskmaster.model.request.DataUpdateJobRequest
import com.dinhtc.taskmaster.model.request.DeleteMaterialRequest
import com.dinhtc.taskmaster.model.request.DeleteMediaRequest
import com.dinhtc.taskmaster.model.request.SearchRequest
import com.dinhtc.taskmaster.model.request.UpdateStateWeightedRequest
import com.dinhtc.taskmaster.model.response.*
import com.dinhtc.taskmaster.utils.ApiResponse
import okhttp3.MultipartBody

interface ApiHelper {
    suspend fun loginUser(userName: String, passWord: String): ApiResponse<Any>
    suspend fun saveFirebaseToken(firebaseToken: String, deviceId: String?, deviceName: String?): ApiResponse<Any>
    suspend fun getListJobType(): ApiResponse<ListJobTypeResponse>
    suspend fun getListEmployeeNotById(id: Int): ApiResponse<ListEmployeeResponse>
    suspend fun getListEmployee(): ApiResponse<ListEmployeeResponse>
    suspend fun getListCollectPoint(): ApiResponse<ListCollectPointResponse>
    suspend fun uploadMultiImageVideo(
        jobId: Int,
        url_image: List<MultipartBody.Part>?,
        url_video: MultipartBody.Part?,
        mediaType: Int
    ): ApiResponse<Any>

    suspend fun uploadVideo(
        jobId: Int, url_video: MultipartBody.Part?, mediaType: Int
    ): ApiResponse<Any>

    suspend fun uploadMultiImage(
        jobId: Int, url_image: List<MultipartBody.Part>?, mediaType: Int
    ): ApiResponse<Any>

    suspend fun addCollectPoint(data: CollectPointRequest): ApiResponse<Any>
    suspend fun addTask(addTaskRequest: AddTaskRequest): ApiResponse<Any>
    suspend fun getListMaterial(): ApiResponse<ListMaterialResponse>
    suspend fun addMaterial(material: AddMaterialRequest): ApiResponse<Any>
    suspend fun getJobDetails(jobId: Int, empId : Int): ApiResponse<JobDetailsResponse>
    suspend fun updateStateWeightedJob(dataUpdate: UpdateStateWeightedRequest): ApiResponse<Any>
    suspend fun deleteMedia(deleteMediaRequest: DeleteMediaRequest): ApiResponse<Any>
    suspend fun deleteMaterial(deleteMaterialRequest: DeleteMaterialRequest): ApiResponse<Any>
    suspend fun getUserProfile(username: String): ApiResponse<Any>
    suspend fun logOut(): ApiResponse<Any>
    suspend fun getListEmployeeByJobId(jdJob: Int): ApiResponse<ListEmployeeResponse>
    suspend fun updateJobDetails(dataUpdate: DataUpdateJobRequest): ApiResponse<Any>
    suspend fun search(searchRequest: SearchRequest): ApiResponse<ListJobSearchResponse>
    suspend fun getCollectPointLatLng(): ApiResponse<ListCollectPointLatLng>
    suspend fun updateStateJobCompactedAndDone(dataLamGonAndDaXong: CompactedAndDoneRequest): ApiResponse<Any>
}