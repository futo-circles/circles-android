package org.futo.circles.provider

import android.content.Context
import kotlinx.coroutines.suspendCancellableCoroutine
import org.matrix.android.sdk.api.Matrix
import org.matrix.android.sdk.api.MatrixConfiguration
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.statistics.StatisticEvent

object MatrixSessionProvider {

    var currentSession: Session? = null
        private set

    fun initSession(context: Context) {
        Matrix(
            context = context, matrixConfiguration = MatrixConfiguration(
                roomDisplayNameFallbackProvider = RoomDisplayNameFallbackProviderImpl()
            )
        ).also { MatrixInstanceProvider.saveMatrixInstance(it) }

        val lastSession = try {
            MatrixInstanceProvider.matrix.authenticationService().getLastAuthenticatedSession()
        } catch (e: Exception) {
            null
        }

        lastSession?.let { startSession(it) }
    }

    fun clearSession() {
        currentSession = null
    }

    private fun startSession(session: Session, listener: Session.Listener? = null) {
        listener?.let { session.addListener(it) }
        currentSession = session.apply { open(); syncService().startSync(true) }
        session.addListener(MatrixSessionListenerProvider.sessionListener)
    }

    suspend fun awaitForSessionStart(session: Session) =
        suspendCancellableCoroutine {
            startSession(session, object : Session.Listener {
                override fun onSessionStarted(session: Session) {
                    super.onSessionStarted(session)
                    it.resume(session) { session.removeListener(this) }
                }
            })
        }

    suspend fun awaitForSessionSync(session: Session) =
        suspendCancellableCoroutine {
            startSession(session, object : Session.Listener {
                override fun onStatisticsEvent(session: Session, statisticEvent: StatisticEvent) {
                    super.onStatisticsEvent(session, statisticEvent)
                    if (statisticEvent is StatisticEvent.InitialSyncRequest)
                        it.resume(session) { session.removeListener(this) }
                }
            })
        }
}