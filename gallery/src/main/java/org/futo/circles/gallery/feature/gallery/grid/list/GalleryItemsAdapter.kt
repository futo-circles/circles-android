package org.futo.circles.gallery.feature.gallery.grid.list

import android.view.View
import android.view.ViewGroup
import org.futo.circles.core.list.BaseRvAdapter
import org.futo.circles.gallery.model.GalleryContentListItem

class GalleryItemsAdapter(
    private val onGalleryItemClicked: (item: GalleryContentListItem, transitionView: View, position: Int) -> Unit,
    private val onLoadMore: () -> Unit
) : BaseRvAdapter<GalleryContentListItem, GalleryItemViewHolder>(DefaultIdEntityCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = GalleryItemViewHolder(
        parent,
        onItemClicked = { position, view ->
            onGalleryItemClicked(getItem(position), view, position)
        }
    )

    override fun onBindViewHolder(holder: GalleryItemViewHolder, position: Int) {
        holder.bind(getItem(position))
        if (position >= itemCount - LOAD_MORE_THRESHOLD) onLoadMore()
    }

    companion object {
        private const val LOAD_MORE_THRESHOLD = 10
    }
}
