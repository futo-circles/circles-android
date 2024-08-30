package org.futo.circles.auth.feature.log_in

import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.futo.circles.auth.R
import org.futo.circles.auth.databinding.FragmentLogInBinding
import org.futo.circles.auth.feature.log_in.suggestion.LoginSuggestionListener
import org.futo.circles.auth.feature.log_in.switch_user.list.SwitchUsersAdapter
import org.futo.circles.auth.model.EmptyUserId
import org.futo.circles.auth.model.ForgotPassword
import org.futo.circles.auth.model.InvalidUserId
import org.futo.circles.auth.model.RemoveUser
import org.futo.circles.auth.model.SuggestedUserId
import org.futo.circles.auth.model.ValidUserId
import org.futo.circles.auth.utils.UserIdUtils
import org.futo.circles.core.base.fragment.BaseBindingFragment
import org.futo.circles.core.base.fragment.HasLoadingState
import org.futo.circles.core.base.list.OffsetDecoration
import org.futo.circles.core.extensions.getText
import org.futo.circles.core.extensions.navigateSafe
import org.futo.circles.core.extensions.observeData
import org.futo.circles.core.extensions.observeResponse
import org.futo.circles.core.extensions.onBackPressed
import org.futo.circles.core.extensions.setIsVisible
import org.futo.circles.core.extensions.showError
import org.futo.circles.core.extensions.withConfirmation


@AndroidEntryPoint
class LogInFragment : BaseBindingFragment<FragmentLogInBinding>(FragmentLogInBinding::inflate),
    HasLoadingState, LoginSuggestionListener {

    override val fragment: Fragment = this
    private val viewModel by viewModels<LogInViewModel>()

    private val switchUsersAdapter by lazy {
        SwitchUsersAdapter(
            onResumeClicked = { id ->
                startLoading(binding.btnLogin)
                viewModel.resumeSwitchUserSession(id)
            },
            onRemoveClicked = { id ->
                withConfirmation(RemoveUser()) {
                    viewModel.removeSwitchUser(id)
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.statusBarColor =
            ContextCompat.getColor(requireContext(), org.futo.circles.core.R.color.dark_background)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setOnClickActions()
        setupObservers()
    }

    private fun setupViews() {
        with(binding) {
            rvSwitchUsers.apply {
                adapter = switchUsersAdapter
                addItemDecoration(OffsetDecoration(16))
            }
            etUserName.filters = arrayOf<InputFilter>(object : InputFilter.AllCaps() {
                override fun filter(
                    source: CharSequence?,
                    start: Int,
                    end: Int,
                    dest: Spanned?,
                    dstart: Int,
                    dend: Int
                ) = source.toString().lowercase()
            })
        }
    }

    private fun setupObservers() {
        viewModel.loginResultLiveData.observeResponse(this,
            success = {
                findNavController().navigateSafe(LogInFragmentDirections.toUiaFragment())
            },
            error = {
                showError(getString(R.string.username_not_found))
            }
        )
        viewModel.switchUsersLiveData.observeData(this) {
            binding.tvResumeSession.setIsVisible(it.isNotEmpty())
            switchUsersAdapter.submitList(it)
        }
        viewModel.navigateToBottomMenuScreenLiveData.observeData(this) {
            findNavController().navigateSafe(LogInFragmentDirections.toHomeFragment())
        }
    }

    private fun setOnClickActions() {
        with(binding) {
            ivBack.setOnClickListener { onBackPressed() }
            btnSignUp.setOnClickListener {
                findNavController().navigateSafe(LogInFragmentDirections.toSignUpFragment())
            }
            btnLogin.setOnClickListener { startLogin(false) }
            btnForgotPassword.setOnClickListener {
                withConfirmation(ForgotPassword()) { startLogin(true) }
            }
        }
    }

    override fun onLoginSuggestionApplied(userId: String, isForgotPassword: Boolean) {
        binding.etUserName.setText(userId)
        loginAs(userId, isForgotPassword)
    }

    private fun startLogin(isForgotPassword: Boolean) {
        val userId = binding.tilUserId.getText()
        when (val status = UserIdUtils.validateUserId(userId)) {
            EmptyUserId -> showError(getString(R.string.user_id_can_not_be_empty))
            InvalidUserId -> showError(getString(R.string.invalid_user_id))
            is SuggestedUserId -> findNavController().navigateSafe(
                LogInFragmentDirections.toLoginSuggestionBottomSheet(
                    status.suggestedUserId, isForgotPassword
                )
            )

            is ValidUserId -> loginAs(userId, isForgotPassword)
        }

    }

    private fun loginAs(userId: String, isForgotPassword: Boolean) {
        startLoading(binding.btnLogin)
        viewModel.startLogInFlow(userId, isForgotPassword)
    }

}