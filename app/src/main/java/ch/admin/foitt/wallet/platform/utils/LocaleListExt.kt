package ch.admin.foitt.wallet.platform.utils

import android.os.LocaleList
import java.util.Locale

fun LocaleList.toListOfLocales(): List<Locale> {
    val list = mutableListOf<Locale>()
    for (i in 0 until this.size()) {
        list.add(this[i])
    }
    return list
}
