package no.feltgis.mobilego.app.network

import io.reactivex.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface Api {

    @GET
    fun get(@Url url: String, @HeaderMap headers: Map<String, String>? = null): Single<Response<ResponseBody>>

    @POST
    fun post(@Url url: String, @Body body: RequestBody, @HeaderMap headers: Map<String, String>? = null): Single<Response<ResponseBody>>

    @PATCH
    fun patch(@Url url: String, @Body body: RequestBody, @HeaderMap headers: Map<String, String>? = null): Single<Response<ResponseBody>>

    @PUT
    fun put(@Url url: String, @Body body: RequestBody, @HeaderMap headers: Map<String, String>? = null): Single<Response<ResponseBody>>

    @DELETE
    fun delete(@Url url: String, @HeaderMap headers: Map<String, String>? = null): Single<Response<ResponseBody>>
}