package com.illill.phairy.data.model

/**
 * @property uid 소유자의 uid
 * @property code 해당 옷/코디의 고유 코드
 */
open class Item {
    var uid: String = ""
    var code: String = ""
    var timestamp: String = ""
    override fun toString(): String {
        return "Item(uid='$uid', code='$code', timestamp='$timestamp')"
    }


}


