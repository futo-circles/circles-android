package org.futo.circles.feature.timeline.preview

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import org.futo.circles.core.extensions.gone
import org.futo.circles.core.extensions.observeData
import org.futo.circles.core.extensions.onBackPressed
import org.futo.circles.core.extensions.showSuccess
import org.futo.circles.core.extensions.visible
import org.futo.circles.core.extensions.withConfirmation
import org.futo.circles.core.fragment.BaseFullscreenDialogFragment
import org.futo.circles.core.share.ShareProvider
import org.futo.circles.databinding.DialogFragmentTimelineMediaPreviewBinding
import org.futo.circles.gallery.R
import org.futo.circles.gallery.feature.gallery.full_screen.media_item.FullScreenMediaFragment
import org.futo.circles.gallery.model.RemoveImage

@AndroidEntryPoint
class TimelineMediaPreviewDialogFragment :
    BaseFullscreenDialogFragment(DialogFragmentTimelineMediaPreviewBinding::inflate) {

    private val viewModel by viewModels<TimelineMediaPreviewViewModel>()
    private val binding by lazy { getBinding() as DialogFragmentTimelineMediaPreviewBinding }
    private val args: TimelineMediaPreviewDialogFragmentArgs by navArgs()

    private val hideHandler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable { hide() }

    private val mediaFragment by lazy { FullScreenMediaFragment.create(args.roomId, args.eventId,0) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.let {
            it.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            it.statusBarColor = Color.BLACK
        }
        setupViews()
        setupToolbar()
        setupObservers()
    }

    private fun setupViews() {
        replaceFragment(mediaFragment)
        binding.lParent.setOnClickListener { toggle() }
        delayedHide()
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.lContainer, fragment)
            .commitAllowingStateLoss()
    }

    @SuppressLint("RestrictedApi")
    private fun setupToolbar() {
        with(binding.toolbar) {
            setNavigationOnClickListener { onBackPressed() }
            (menu as? MenuBuilder)?.setOptionalIconsVisible(true)
            setOnMenuItemClickListener { item ->
                return@setOnMenuItemClickListener when (item.itemId) {
                    R.id.save -> {
                        viewModel.save()
                        true
                    }

                    R.id.share -> {
                        viewModel.share()
                        true
                    }

                    R.id.delete -> {
                        withConfirmation(RemoveImage()) {
                            viewModel.removeImage()
                            onBackPressed()
                        }
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.shareLiveData.observeData(this) { content ->
            context?.let { ShareProvider.share(it, content) }
        }
        viewModel.downloadLiveData.observeData(this) {
            context?.let { showSuccess(it.getString(R.string.saved)) }
        }
    }

    private fun toggle() {
        if (binding.toolbar.isVisible) hide() else show()
    }

    private fun hide() {
        binding.toolbar.gone()
    }

    private fun show() {
        binding.toolbar.visible()
        delayedHide()
    }

    private fun delayedHide() {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, AUTO_HIDE_DELAY_MILLIS)
    }

    companion object {
        private const val AUTO_HIDE_DELAY_MILLIS = 3000L
    }
}