package com.illill.phairy.data.model

import com.illill.phairy.BaseApplication.Companion.STORAGE_URL

/**
 * 고객의 정보를 담는 클래스
 *
 * ㅡ사람 정보
 * @property uid,token,nickname 등 사람 정보
 *
 * ㅡ사용 정보
 * @property clothCount 옷 개수
 * @property codiCount 코디 개수
 * @property followers 나를 따르는 팔로워들의 리스트
 * @property followings 내가 따르는 팔로잉들 리스트
 * @property open 공개여부
 */
class Client {
    var uid: String = "guest"
    var token = ""
    var nickname = "guest"
    val profileUrl: String
        get() = "$STORAGE_URL/profiles/$uid"

    var followers = listOf<String>()
    var followings = listOf<String>()


    var open: Boolean = true

//    var favorites = listOf<String>()

//    enum  class Season(i: Int) {
//        Spring(0b1000), Summer(0b0100), Fall(0b0010), Winter(0b0001);
//        fun contains(season:Int){
//            when{
//                season and Spring !=0-> Spring
//            }
//        }
//    }

    var style: String = ""
    var brand: String = ""
    var color: String = ""

    override fun toString(): String {
        return "Client(uid='$uid', token='$token', nickname='$nickname', followers=$followers, followings=$followings,  style='$style', brand='$brand', color='$color')"
    }


}

