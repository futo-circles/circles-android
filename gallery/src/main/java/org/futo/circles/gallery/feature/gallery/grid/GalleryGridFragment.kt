package org.futo.circles.gallery.feature.gallery.grid

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.View.OnLayoutChangeListener
import androidx.core.app.SharedElementCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.transition.TransitionInflater
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.futo.circles.core.extensions.observeData
import org.futo.circles.core.extensions.setIsVisible
import org.futo.circles.core.list.BaseRvDecoration
import org.futo.circles.core.picker.DeviceMediaPickerHelper.Companion.IS_VIDEO_AVAILABLE
import org.futo.circles.core.picker.MediaType
import org.futo.circles.gallery.R
import org.futo.circles.gallery.databinding.FragmentGalleryGridBinding
import org.futo.circles.gallery.feature.gallery.GalleryMediaPreviewListener
import org.futo.circles.gallery.feature.gallery.full_screen.FullScreenPagerFragment.Companion.POSITION
import org.futo.circles.gallery.feature.gallery.grid.list.GalleryItemViewHolder
import org.futo.circles.gallery.feature.gallery.grid.list.GalleryItemsAdapter
import org.futo.circles.gallery.feature.pick.AllMediaPickerHelper
import org.futo.circles.gallery.feature.pick.PickGalleryMediaListener
import org.futo.circles.gallery.model.GalleryContentListItem


@AndroidEntryPoint
class GalleryGridFragment : Fragment(R.layout.fragment_gallery_grid) {

    private val viewModel by viewModels<GalleryViewModel>({ requireParentFragment() })
    private val binding by viewBinding(FragmentGalleryGridBinding::bind)
    private val mediaPickerHelper = AllMediaPickerHelper(this, true)
    private val listAdapter by lazy {
        GalleryItemsAdapter(
            onGalleryItemClicked = { item, view, position ->
                onMediaItemSelected(item, view, position)
            },
            onLoadMore = { viewModel.loadMore() })
    }

    private var pickMediaListener: PickGalleryMediaListener? = null
    private var previewMediaListener: GalleryMediaPreviewListener? = null
    private var returnViewPosition = -1

    override fun onAttach(context: Context) {
        super.onAttach(context)
        pickMediaListener = parentFragment as? PickGalleryMediaListener
        previewMediaListener = parentFragment as? GalleryMediaPreviewListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        prepareTransitions()
        setupObservers()
        parentFragmentManager.setFragmentResultListener(POSITION, this) { _, bundle ->
            returnViewPosition = bundle.getInt(POSITION)
            scrollToReturnPosition()
        }
    }

    private fun prepareTransitions() {
        exitTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.grid_exit_transition)
        setExitSharedElementCallback(
            object : SharedElementCallback() {
                override fun onMapSharedElements(
                    names: List<String?>,
                    sharedElements: MutableMap<String?, View?>
                ) {
                    val selectedViewHolder: RecyclerView.ViewHolder =
                        binding.rvGallery.getRecyclerView()
                            .findViewHolderForAdapterPosition(returnViewPosition) ?: return

                    val t = selectedViewHolder.itemView.findViewById<View>(R.id.ivCover)
                    sharedElements[names[0]] = t
                    returnViewPosition = -1
                }
            })
        postponeEnterTransition()
    }

    private fun setupViews() {
        binding.rvGallery.apply {
            layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = listAdapter
            addItemDecoration(BaseRvDecoration.OffsetDecoration<GalleryItemViewHolder>(2))
            bindToFab(binding.fbUploadImage)
        }
        binding.fbUploadImage.setOnClickListener { showImagePicker() }
        binding.fbUploadImage.setIsVisible(pickMediaListener == null)
    }

    private fun setupObservers() {
        viewModel.galleryItemsLiveData.observeData(this) {
            listAdapter.submitList(it)
        }
    }

    private fun scrollToReturnPosition() {
        val recyclerView = binding.rvGallery.getRecyclerView()
        recyclerView.addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int,
                oldTop: Int, oldRight: Int, oldBottom: Int
            ) {
                recyclerView.removeOnLayoutChangeListener(this)
                val layoutManager =
                    (recyclerView.layoutManager as? StaggeredGridLayoutManager) ?: return
                val viewAtPosition = layoutManager.findViewByPosition(returnViewPosition)
                val isItemVisible =
                    viewAtPosition==null ||
                    layoutManager.isViewPartiallyVisible(viewAtPosition, false, true)

                recyclerView.post {
                    if (isItemVisible) layoutManager.scrollToPositionWithOffset(
                        returnViewPosition, 5
                    )
                    startPostponedEnterTransition()
                }
            }
        })
    }

    private fun showImagePicker() {
        mediaPickerHelper.showMediaPickerDialog(
            onImageSelected = { _, uri ->
                viewModel.uploadMedia(uri, MediaType.Image)
            },
            onVideoSelected = { uri ->
                viewModel.uploadMedia(uri, MediaType.Video)
            }
        )
    }

    private fun onMediaItemSelected(item: GalleryContentListItem, view: View, position: Int) {
        pickMediaListener?.let {
            viewModel.selectMediaForPicker(requireContext(), item, it)
        } ?: previewMediaListener?.onPreviewMedia(item.id, view, position)
    }

    companion object {
        private const val ROOM_ID = "roomId"
        fun create(roomId: String, isVideoAvailable: Boolean) = GalleryGridFragment().apply {
            arguments = bundleOf(
                ROOM_ID to roomId,
                IS_VIDEO_AVAILABLE to isVideoAvailable
            )
        }
    }
}
