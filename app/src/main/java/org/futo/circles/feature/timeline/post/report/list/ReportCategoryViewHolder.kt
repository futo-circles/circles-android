package org.futo.circles.feature.timeline.post.report.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.futo.circles.core.list.ViewBindingHolder
import org.futo.circles.databinding.ReportCategoryListItemBinding
import org.futo.circles.extensions.onClick
import org.futo.circles.model.ReportCategoryListItem

class ReportCategoryViewHolder(
    parent: ViewGroup,
    onCategorySelected: (Int) -> Unit
) : RecyclerView.ViewHolder(inflate(parent, ReportCategoryListItemBinding::inflate)) {

    private companion object : ViewBindingHolder

    private val binding = baseBinding as ReportCategoryListItemBinding

    init {
        onClick(binding.lItem) { position -> onCategorySelected(position) }
    }

    fun bind(data: ReportCategoryListItem) {
        binding.radio.text = data.name
        binding.radio.isChecked = data.isSelected
    }
}