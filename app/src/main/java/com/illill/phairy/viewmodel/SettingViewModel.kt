package com.illill.phairy.viewmodel

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.illill.phairy.BR
import com.illill.phairy.BaseApplication.Companion.sharedPreferences

/**
 * 사용자의 정보를 업데이트한다
 */
class SettingViewModel() : BaseObservable() {
    private val TAG = this.javaClass.simpleName

    @get: Bindable
    var notificationEnabled = sharedPreferences.getBoolean("notificationEnabled", true)
    set(value) {
        field = value
        sharedPreferences.edit().putBoolean("notificationEnabled", value).apply()
        notifyPropertyChanged(BR.notificationEnabled)
    }

}
