package com.codinglikeapirate.pocitaj

import android.app.Application

open class PocitajApplication : Application() {
    open val inkModelManager: InkModelManager by lazy {
        ModelManager()
    }
}
