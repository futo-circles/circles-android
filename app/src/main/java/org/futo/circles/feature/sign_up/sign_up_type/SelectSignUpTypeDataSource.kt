package org.futo.circles.feature.sign_up.sign_up_type

import android.content.Context
import org.futo.circles.R
import org.futo.circles.core.REGISTRATION_SUBSCRIPTION_TYPE
import org.futo.circles.core.REGISTRATION_TOKEN_TYPE
import org.futo.circles.core.utils.HomeServerUtils.buildHomeServerConfigFromDomain
import org.futo.circles.extensions.createResult
import org.futo.circles.feature.sign_up.SignUpDataSource
import org.futo.circles.provider.MatrixInstanceProvider
import org.matrix.android.sdk.api.auth.registration.Stage

class SelectSignUpTypeDataSource(
    private val context: Context,
    private val signUpDataSource: SignUpDataSource
) {

    private val authService by lazy { MatrixInstanceProvider.matrix.authenticationService() }

    fun clearSubtitle() {
        signUpDataSource.clearSubtitle()
    }

    suspend fun startNewRegistration(
        name: String,
        domain: String,
        isSubscription: Boolean,
        subscriptionReceipt: String?
    ) = createResult {
        authService.cancelPendingLoginOrRegistration()
        authService.initiateAuth(buildHomeServerConfigFromDomain(domain))
        val flows = authService.getRegistrationWizard().getAllRegistrationFlows()
        signUpDataSource.startSignUpStages(
            prepareStagesList(isSubscription, flows),
            name,
            domain,
            subscriptionReceipt
        )
    }

    private fun prepareStagesList(isSubscription: Boolean, flows: List<List<Stage>>): List<Stage> =
        if (isSubscription) {
            flows.firstOrNull {
                (it.firstOrNull() as? Stage.Other)?.type == REGISTRATION_SUBSCRIPTION_TYPE
            }
        } else {
            flows.firstOrNull {
                (it.firstOrNull() as? Stage.Other)?.type == REGISTRATION_TOKEN_TYPE
            }
        } ?: throw IllegalArgumentException(context.getString(R.string.wrong_signup_config))
}