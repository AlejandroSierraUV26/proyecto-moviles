package com.example.proyecto.data.api

import android.app.Application
import android.content.Context
import android.net.Uri
import com.example.proyecto.data.models.UserRegisterRequest
import com.example.proyecto.data.models.UserLoginRequest
import com.example.proyecto.data.models.LoginResponse
import com.example.proyecto.data.models.RegisterResponse
import com.example.proyecto.data.models.UserProfile
import com.example.proyecto.data.models.ApiResponse
import com.example.proyecto.data.models.ApiResponseWithData
import com.example.proyecto.data.models.Course
import com.example.proyecto.data.models.DiagnosticQuestion
import com.example.proyecto.data.models.DiagnosticResult
import com.example.proyecto.data.models.DiagnosticSubmission
import com.example.proyecto.data.models.Section
import com.example.proyecto.data.models.Exam
import com.example.proyecto.data.models.ExperienceData
import com.example.proyecto.data.models.ExperienceTotalResponse
import com.example.proyecto.data.models.ExamFeedbackResult
import com.example.proyecto.data.models.ExamResult
import com.example.proyecto.data.models.ExamSubmission
import com.example.proyecto.data.models.GoogleAuthResponse
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import java.io.File
import java.io.FileOutputStream
import com.example.proyecto.data.models.GoogleAuthRequest
import retrofit2.http.Headers

interface ApiService {
    @POST("api/register")
    suspend fun registerUser(@Body request: UserRegisterRequest): Response<RegisterResponse>

    @POST("api/login")
    suspend fun loginUser(@Body request: UserLoginRequest): Response<LoginResponse>

    @POST("/api/auth/google")
    suspend fun authWithGoogle(@Body request: GoogleAuthRequest): Response<ApiResponseWithData<GoogleAuthResponse>>

    @GET("api/profile")
    suspend fun getUserProfile(): Response<UserProfile>

    @PUT("api/profile/update")
    suspend fun updateProfile(@Body request: Map<String, String>): Response<ApiResponse>

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

    @PUT("api/experience/update")
    suspend fun updateExperience(@Body request: Map<String, String>): ApiResponse

    @GET("api/streak")
    suspend fun getUserStreak(): Map<String, Int>
 
    @GET("api/exams/questions/{examId}")
    suspend fun getQuestionsByExam(@Path("examId") examId: Int): Response<List<Question>>

    @POST("api/exams/questions/create")
    suspend fun createQuestion(@Body request: QuestionRequest): Response<ApiResponse>

    @POST("/api/exams/submit") // ← Ruta correcta
    suspend fun evaluateExam(@Body submission: ExamSubmission): Response<ExamFeedbackResult>

    @POST("api/password/forgot")
    suspend fun sendPasswordResetEmail(@Body request: Map<String, String>): ApiResponse

    @POST("api/password/reset")
    suspend fun resetPassword(@Body request: Map<String, String>): ApiResponse

    @GET("api/profile/image")
    suspend fun getProfileImage(): ProfileImageResponse

    @POST("api/exams/diagnostic")  // Asegúrate que coincida con tu backend
    suspend fun submitDiagnostic(
        @Header("Authorization") token: String,
        @Body submission: DiagnosticSubmission
    ): Response<DiagnosticResult>


    @GET("exams/diagnostic-result/{courseId}/{level}")
    suspend fun getDiagnosticResult(
        @Path("courseId") courseId: Int,
        @Path("level") level: String
    ): Response<DiagnosticResult>



    // Para obtener preguntas del diagnóstico (si lo implementas)
    @GET("api/exams/diagnostic/questions")
    suspend fun getDiagnosticQuestions(
        @Header("Authorization") token: String,
        @Query("courseId") courseId: Int,
        @Query("level") level: Int
    ): Response<List<DiagnosticQuestion>>


    // Para enviar resultados del diagnóstico (como lo tienes en backend)
    @POST("api/exams/diagnostic/submit")
    suspend fun submitDiagnosticResults(
        @Header("Authorization") token: String,
        @Body submission: DiagnosticSubmission
    ): Response<DiagnosticResult>


    @Multipart
    @PUT("api/profile/image")
    suspend fun updateProfileImage(
        @Part image: MultipartBody.Part 
    ): ProfileImageResponse

    @DELETE("api/profile/image")
    suspend fun deleteProfileImage(): ProfileImageResponse


    companion object {
        fun createImagePart(uri: Uri, context: Context): MultipartBody.Part {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_image.jpg")
            FileOutputStream(file).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }

            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            return MultipartBody.Part.createFormData("image", file.name, requestFile)
        }
    }
}

data class ProfileImageResponse(
    val success: Boolean,
    val message: String,
    val imageUrl: String?
)

