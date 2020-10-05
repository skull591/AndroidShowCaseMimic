package edu.nju.ics.alex.wang.androidshowcasemimic.app

import android.content.Context
import com.facebook.stetho.Stetho
import com.google.android.play.core.splitcompat.SplitCompatApplication
import edu.nju.ics.alex.wang.androidshowcasemimic.BuildConfig
import edu.nju.ics.alex.wang.androidshowcasemimic.appModule
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import timber.log.Timber

class ShowCaseApplication : SplitCompatApplication(), KodeinAware {
    override val kodein = Kodein.lazy {
        import(androidXModule(this@ShowCaseApplication))
        import(appModule)
        import(baseModule)
        importAll(FeatureManager.kodeinModules)

        externalSources.add(FragmentArgsExternalSourc())
    }

    private lateinit var context : Context

    override fun onCreate() {
        super.onCreate()

        context = this

        initStetho()
        initTimber()
    }

    private fun initStetho() {
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}