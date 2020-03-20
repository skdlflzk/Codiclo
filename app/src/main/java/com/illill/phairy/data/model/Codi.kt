package com.illill.phairy.data.model


/**
 * 코디 정보
 * ㅡ코디 관련
 * @property code 코디의 고유 코드
 * @property uid 해당 코디 주인의 uid
 *
 * ㅡ코디 데이터
 * @property tags ex)슬랙스, 7부, 판매처, 등등
 * @property clothes 추가된 옷들의 고유 코드 리스트
 * @property positions 옷이 태그된 태그 위치. clothes와 같은 순서 ex) ["0.25,0.12","0.1,0.9"]
 * @property pictures 사진 리스트
 * @property season 봄,여름,가을,겨울. 중복 가능 SPRING(0b0001), SUMMER(0b0010), FALL(0b0100), WINTER(0b1000)
 * @property memo 코디에 쓴 문구
 * @property open 공개여부
 *
 */
class Codi : Item() {
    var nickname: String = ""

    var memo: String = ""

    var tags: List<String> = listOf()
    var clothes: List<String> = listOf()
    var pictures: List<String> = listOf()
    var season: Int = 0
    var open: Boolean = true


    var fav = 0
    override fun toString(): String {
        return "Codi(code='$code', uid='$uid',timestamp = '$timestamp' nickname='$nickname', memo='$memo', tags=$tags, clothes=$clothes, pictures=$pictures, season=$season, open=$open, fav=$fav)"
    }


}
