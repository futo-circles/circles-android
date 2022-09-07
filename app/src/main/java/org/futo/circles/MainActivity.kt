package org.futo.circles

import android.content.Intent
import org.futo.circles.core.BaseActivity
import org.futo.circles.provider.MatrixSessionProvider


class MainActivity : BaseActivity(R.layout.activity_main) {

    // Special action to clear cache and/or clear credentials (Element workaround to clear database)
    fun restartForLogout() {
        MatrixSessionProvider.clearSession()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        this.startActivity(intent)
    }
}