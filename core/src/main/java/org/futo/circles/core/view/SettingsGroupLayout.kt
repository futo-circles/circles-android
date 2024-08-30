package org.futo.circles.core.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import org.futo.circles.core.R
import org.futo.circles.core.databinding.LayoutSettingsGroupBinding
import org.futo.circles.core.extensions.getAttributes
import org.futo.circles.core.extensions.gone
import org.futo.circles.core.extensions.visible


class SettingsGroupLayout(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    private val binding =
        LayoutSettingsGroupBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
        getAttributes(attrs, R.styleable.SettingsGroupLayout) {
            getString(R.styleable.SettingsGroupLayout_groupName)?.let {
                binding.tvGroupTitle.apply {
                    text = it
                    binding.tvGroupTitle.visible()
                }
            } ?: binding.tvGroupTitle.gone()
        }
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        val id = child.id
        if (id == R.id.tvGroupTitle || id == R.id.cvGroupBox || id == R.id.lItemsContainer) {
            super.addView(child, index, params)
        } else {
            binding.lItemsContainer.addView(child, index, params)
        }
    }
}