package org.futo.circles.feature.direct.timeline.list.holder

import android.view.ViewGroup
import org.futo.circles.core.base.list.ViewBindingHolder
import org.futo.circles.core.databinding.ListItemTimelineLoadingBinding
import org.futo.circles.core.model.DmTimelineListItem
import org.futo.circles.core.model.DmTimelineLoadingItem


class DmTimelineLoadingViewHolder(
    parent: ViewGroup,
) : DmTimelineListItemViewHolder(inflate(parent, ListItemTimelineLoadingBinding::inflate)) {

    private companion object : ViewBindingHolder

    override fun bind(
        item: DmTimelineListItem, previousItem: DmTimelineListItem?,
        nextItem: DmTimelineListItem?
    ) {
        if (item !is DmTimelineLoadingItem) return
    }
}