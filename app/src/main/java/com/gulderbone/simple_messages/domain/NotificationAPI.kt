package com.gulderbone.simple_messages.domain

import com.gulderbone.simple_messages.models.PushNotification
import com.gulderbone.simple_messages.utils.Constants.CONTENT_TYPE
import com.gulderbone.simple_messages.utils.Constants.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>
}