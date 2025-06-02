package com.example.proyecto.data.api

import com.example.proyecto.data.models.UserRegisterRequest
import com.example.proyecto.data.models.UserLoginRequest
import com.example.proyecto.data.models.LoginResponse
import com.example.proyecto.data.models.RegisterResponse
import com.example.proyecto.data.models.UserProfile
import com.example.proyecto.data.models.ApiResponse
import com.example.proyecto.data.models.Course
import com.example.proyecto.data.models.Section
import com.example.proyecto.data.models.Exam
import com.example.proyecto.data.models.ExperienceData
import com.example.proyecto.data.models.ExperienceTotalResponse
import com.example.proyecto.data.models.ExamFeedbackResult
import com.example.proyecto.data.models.ExamResult
import com.example.proyecto.data.models.ExamSubmission
import com.example.proyecto.data.models.Question
import com.example.proyecto.data.models.QuestionRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Path

interface ApiService {
    @POST("api/register")
    suspend fun registerUser(@Body request: UserRegisterRequest): Response<RegisterResponse>

    @POST("api/login")
    suspend fun loginUser(@Body request: UserLoginRequest): Response<LoginResponse>

    @GET("api/profile")
    suspend fun getUserProfile(): Response<UserProfile>

    @PUT("api/profile/update")
    suspend fun updateProfile(
        @Body request: Map<String, String>): Response<ApiResponse>

    @DELETE("api/delete")
    suspend fun deleteAccount(
        @Header("Authorization") token: String,
        @Query("password") password: String): ApiResponse

    @GET("api/exams/courses")
    suspend fun getAllCourses(): List<Course>

    @GET("api/courses")
    suspend fun getUserCourses(): List<Course>

    @POST("api/courses/add")
    suspend fun addCourseToUser(@Body request: Map<String, Int>)

    @POST("api/courses/remove")
    suspend fun removeCourseFromUser(@Body request: Map<String, Int>)

    @GET("api/exams/sections/{courseId}")
    suspend fun getSectionsByCourse(@Path("courseId") courseId: Int): Response<List<Section>>

    @GET("api/exams/by-section/{sectionId}")
    suspend fun getExamsBySection(@Path("sectionId") sectionId: Int): Response<List<Exam>>


    @GET("api/experience")
    suspend fun getUserExperience(): ExperienceTotalResponse

    @GET("api/experience/last7")
    suspend fun getLast7DaysExperience(): List<ExperienceData>

    @GET("api/exams/questions/{examId}")
    suspend fun getQuestionsByExam(@Path("examId") examId: Int): Response<List<Question>>

    @POST("api/exams/questions/create")
    suspend fun createQuestion(@Body request: QuestionRequest): Response<ApiResponse>

    @POST("submit") // ← Ruta correcta
    suspend fun evaluateExam(@Body submission: ExamSubmission): Response<ExamResult>
}

