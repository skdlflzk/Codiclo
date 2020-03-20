package com.illill.phairy.data.model

import com.illill.phairy.BaseApplication.Companion.STORAGE_URL

/**
 * @property uid 소유자의 uid
 * @property code 해당 옷의 고유 코드
 * @property memo 옷에 대한 개인 평가
 * @property tags ex)슬랙스, 7부, 판매처, 등등
 * @property category ex)TOP,BOTTOM,...
 * @property season 봄,여름,가을,겨울. 중복 가능 SPRING(0b0001), SUMMER(0b0010), FALL(0b0100), WINTER(0b1000)
 *


//see var color: Int = 0
 */
class Cloth : Item() {
    var nickname: String = ""

    var memo: String = ""
    var category: String = ""

    var tags: List<String> = listOf()
    var season: Int = 0
    val iconUrl: String
        get() = "$STORAGE_URL/clothes/$code"
    var open: Boolean = true

    var fav = 0
    var link: String = ""

    override fun toString(): String {
        return "Cloth(nickname='$nickname', memo='$memo', category='$category', tags=$tags, season=$season, open=$open, fav=$fav, link='$link')"
    }


}

