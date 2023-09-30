package com.elogictics.taskmaster.service

import com.elogictics.taskmaster.model.request.AddMaterialRequest
import com.elogictics.taskmaster.model.request.AddTaskRequest
import com.elogictics.taskmaster.model.request.CollectPointRequest
import com.elogictics.taskmaster.model.request.CompactedAndDoneRequest
import com.elogictics.taskmaster.model.request.DataUpdateJobRequest
import com.elogictics.taskmaster.model.request.DeleteMaterialRequest
import com.elogictics.taskmaster.model.request.DeleteMediaRequest
import com.elogictics.taskmaster.model.response.*
import com.elogictics.taskmaster.model.request.LoginRequest
import com.elogictics.taskmaster.model.request.SearchRequest
import com.elogictics.taskmaster.model.request.UpdateStateWeightedRequest
import com.elogictics.taskmaster.utils.ApiResponse
import com.elogictics.taskmaster.utils.SharedPreferencesManager
import okhttp3.MultipartBody
import javax.inject.Inject

class ApiHelperImpl @Inject constructor(private val apiService: ApiService) : ApiHelper {
    override suspend fun loginUser(userName: String, passWord: String): ApiResponse<Any> {
        val loginRequest = LoginRequest(userName, passWord)
        return apiService.loginUser(loginRequest)
    }

    override suspend fun saveFirebaseToken(
        firebaseToken: String,
        deviceId: String?,
        deviceName: String?
    ): ApiResponse<Any> {
        return apiService.saveFirebaseToken(firebaseToken, deviceId, deviceName)
    }

    override suspend fun getListJobType(): ApiResponse<ListJobTypeResponse> {
        return apiService.getListJobType()
    }

    override suspend fun getListEmployeeNotById(id: Int): ApiResponse<ListEmployeeResponse> {
        return apiService.getListEmployeeNotById(id)
    }

    override suspend fun getListEmployee(): ApiResponse<ListEmployeeResponse> {
        return apiService.getListEmployee()
    }

    override suspend fun getListCollectPoint(): ApiResponse<ListCollectPointResponse> {
        return apiService.getListCollectPoint()
    }

    override suspend fun uploadMultiImageVideo(
        jobId: Int,
        url_image: List<MultipartBody.Part>?,
        url_video: MultipartBody.Part?,
        mediaType: Int
    ): ApiResponse<Any> {
        return apiService.uploadMultiImageVideo(jobId, url_image, url_video, mediaType)
    }

    override suspend fun uploadVideo(
        jobId: Int, url_video: MultipartBody.Part?, mediaType: Int
    ): ApiResponse<Any> {
        return apiService.uploadVideo(jobId, url_video, mediaType)
    }

    override suspend fun uploadMultiImage(
        jobId: Int, url_image: List<MultipartBody.Part>?, mediaType: Int
    ): ApiResponse<Any> {
        return apiService.uploadMultiImage(jobId, url_image, mediaType)
    }


    override suspend fun addCollectPoint(data: CollectPointRequest): ApiResponse<Any> {
        return apiService.addCollectPoint(data)
    }

    override suspend fun addTask(addTaskRequest: AddTaskRequest): ApiResponse<Any> {
        return apiService.addTask(addTaskRequest)
    }

    override suspend fun getListMaterial(): ApiResponse<ListMaterialResponse> {
        return apiService.getListMaterial()
    }

    override suspend fun addMaterial(material: AddMaterialRequest): ApiResponse<Any> {
        return apiService.addMaterial(material)
    }

    override suspend fun getJobDetails(jobId: Int, empId: Int): ApiResponse<JobDetailsResponse> {
        return apiService.getJobDetails(jobId, empId)
    }

    override suspend fun updateStateWeightedJob(
        dataUpdate : UpdateStateWeightedRequest
    ): ApiResponse<Any> {
        return apiService.updateStateWeightedJob(dataUpdate)
    }

    override suspend fun deleteMedia(deleteMediaRequest: DeleteMediaRequest): ApiResponse<Any> {
        return apiService.deleteMedia(deleteMediaRequest)
    }

    override suspend fun deleteMaterial(deleteMaterialRequest: DeleteMaterialRequest): ApiResponse<Any> {
        return apiService.deleteMaterial(deleteMaterialRequest)
    }

    override suspend fun getUserProfile(username: String): ApiResponse<Any> {
        return apiService.getUserProfile(username)
    }

    override suspend fun logOut(): ApiResponse<Any> {
        val token = "Bearer " + SharedPreferencesManager.instance.getString(SharedPreferencesManager.TOKEN_LOGIN, "")
        return apiService.loginOut(token)
    }

    override suspend fun getListEmployeeByJobId(jobId: Int): ApiResponse<ListEmployeeResponse> {
        return apiService.getListEmployeeByJobId(jobId)
    }

    override suspend fun updateJobDetails(dataUpdate: DataUpdateJobRequest): ApiResponse<Any> {
        return apiService.updateJobDetails(dataUpdate)
    }

    override suspend fun search(searchRequest: SearchRequest): ApiResponse<ListJobSearchResponse> {
        var startDate = searchRequest.startDate
        var endDate = searchRequest.endDate
        var empStatus = searchRequest.empStatus
        var empId = searchRequest.empId
        var status = searchRequest.status
        var paymentStatus = searchRequest.paymentStatus
        var jobId = searchRequest.jobId
        var empRequest = searchRequest.empRequest
        var collectPoint = searchRequest.collectPoint

        return apiService.search(
            startDate,
            endDate,
            empStatus,
            empId,
            status,
            paymentStatus,
            jobId,
            empRequest,
            collectPoint
        )
    }

    override suspend fun getCollectPointLatLng(): ApiResponse<ListCollectPointLatLng> {
       return apiService.getCollectPointLatLng()
    }

    override suspend fun updateStateJobCompactedAndDone(dataLamGonAndDaXong: CompactedAndDoneRequest): ApiResponse<Any> {
        return apiService.updateStateJobCompactedAndDone(dataLamGonAndDaXong)
    }

}