package com.illill.phairy.util

import com.illill.phairy.data.model.Client
import com.illill.phairy.data.model.Cloth
import com.illill.phairy.data.model.Codi
import com.illill.phairy.data.model.Item

import io.reactivex.Observable
import kotlinx.coroutines.Deferred
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface RetrofitClient {


    /**
     * http://cadmin80.blogspot.com/2017/09/db-mysql-user.html
     * ######################## CLIENT
     */
    @GET("clients/")
    fun loadClientAsync(@Query("uid") uid: String): Deferred<Client>

    //최근 로그인 정보를 확인하기 위해 따로 저장
    @GET("clients/login")
    fun loadSelfAsync(@Query("uid") uid: String): Deferred<Client>

    @FormUrlEncoded
    @POST("clients/")
    fun joinClientAsync(@Field("client") clients: String): Deferred<Boolean>


    @Multipart
    @POST("clients/property/")
    fun updateProfileAsync(@Part profile: MultipartBody.Part): Deferred<Boolean>
//    fun updateProfileAsync(@Part profile: MultipartBody.Part, @Path("uid") uid: String): Deferred<Boolean>

    @PUT("clients/property")
    fun updatePropertyAsync(@Query("uid") uid: String, @Query("property") property: String, @Query("value") value: String): Deferred<Boolean>


    @PUT("clients/open")
    fun updateOpenAsync(@Query("uid") uid: String, @Query("property") property: String, @Query("value") value: String): Deferred<Boolean>


    // uid리스트로 client 정보를 가져옴
    @GET("clients/nickname")
    fun loadNicknamesAsync(@Query("uidList") uid: List<String>): Deferred<List<Client>>

    // destUid를 팔로우한다
    @PUT("clients/follow")
    fun followAsync(@Query("srcUid") src: String,@Query("destUid") dst: String): Deferred<Boolean>

    // destUid를 언팔한다
    @PUT("clients/unfollow")
    fun unfollowAsync(@Query("srcUid") src: String,@Query("destUid") dst: String): Deferred<Boolean>











    @GET("clients/fav")
    fun loadFavByUidAsync(@Query("uid") uid: String): Deferred<List<Any>>  //uid의 최애템 가져오기


    /**
     * ########################CLOTH
     */
    @Multipart
    @POST("clothes/")
    fun addClothAsync(@Part icon: Array<MultipartBody.Part>, @Part("clothJson") json: RequestBody): Deferred<Boolean>

    @PUT("clothes/")
    fun updateClothAsync(@Query("clothJson") json: String): Deferred<Boolean>

    @DELETE("clothes/")
    fun deleteClothAsync(@Query("clothJson") json: String): Deferred<Boolean>

    @GET("clothes/")
    fun loadClothByUidAsync(@Query("uid") query: String): Deferred<List<Cloth>>

    @GET("clothes/tag")
    fun loadClothByTagAsync(@Query("query") query: String): Deferred<List<Cloth>>

    @GET("clothes/code")
    fun loadClothByCodeAsync(@Query("code") code: String): Deferred<Cloth>



    @PUT("clothes/addFav")
    fun addFavClothAsync(@Query("uid") uid: String,@Query("code") code: String): Deferred<Boolean>

    @PUT("clothes/subFav")
    fun subFavClothAsync(@Query("uid") uid: String,@Query("code") code: String): Deferred<Boolean>

    /**
     * ######################## CODI
     */
    @Multipart
    @POST("codi/")
    fun addCodiAsync(@Part icon: Array<MultipartBody.Part>, @Part("codiJson") json: RequestBody): Deferred<Boolean>

    @PUT("codi/")
    fun updateCodiAsync(@Query("codiJson") json: String): Deferred<Boolean>

    @DELETE("codi/")
    fun deleteCodiAsync(@Query("codiJson") json: String): Deferred<Boolean>

    @GET("codi/")
    fun loadCodiByUidAsync(@Query("uid") uid: String): Deferred<List<Codi>>  //사람이 올린 결과를 가져옴

    @GET("codi/tag/")
    fun loadCodiByTagAsync(@Query("query") query: String): Deferred<List<Codi>>  //태그로 검색한 결과 가져옴


    @PUT("codi/addFav")
    fun addFavCodiAsync(@Query("uid") uid: String,@Query("code") code: String): Deferred<Boolean>

    @PUT("codi/subFav")
    fun subFavCodiAsync(@Query("uid") uid: String,@Query("code") code: String): Deferred<Boolean>

    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ체류 측정ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

    @GET("users/resident")
    fun resident(@Query("nickname") nickname: String, @Query("time") time: Int): Observable<Boolean>


}
