package org.futo.circles.feature.timeline.post.emoji

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import org.futo.circles.databinding.BottomSheetEmojiBinding
import org.futo.circles.extensions.observeData
import org.futo.circles.feature.timeline.post.emoji.list.EmojiAdapter
import org.futo.circles.model.EmojiCategory
import org.futo.circles.model.EmojiItem
import org.koin.androidx.viewmodel.ext.android.viewModel

interface EmojiPickerListener {
    fun onEmojiSelected(roomId: String, eventId: String, emoji: String)
}

class EmojiBottomSheet : BottomSheetDialogFragment() {

    private var binding: BottomSheetEmojiBinding? = null
    private var emojiPickerListener: EmojiPickerListener? = null
    private val args: EmojiBottomSheetArgs by navArgs()
    private val viewModel by viewModel<EmojiViewModel>()
    private val listAdapter by lazy { EmojiAdapter(::onEmojiSelected) }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        emojiPickerListener =
            parentFragmentManager.fragments.firstOrNull { it is EmojiPickerListener } as? EmojiPickerListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetEmojiBinding.inflate(inflater, container, false)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as? BottomSheetDialog)?.let {
            it.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        binding?.apply {
            ivClose.setOnClickListener { dismiss() }
            rvEmoji.adapter = listAdapter
        }
    }

    private fun setupObservers() {
        viewModel.categoriesLiveData.observeData(this) { setupEmojiCategories(it) }
        viewModel.emojiesForCategoryLiveData.observeData(this) {
            listAdapter.submitList(it)
        }
    }

    private fun onEmojiSelected(emojiItem: EmojiItem) {
        emojiPickerListener?.onEmojiSelected(args.roomId, args.eventId, emojiItem.emoji)
        dismiss()
    }

    private fun setupEmojiCategories(categories: List<EmojiCategory>) {
        binding?.let { binding ->
            categories.forEach { category ->
                binding.tabs.newTab().apply {
                    tag = category.id
                    text = category.emojiTitle
                    contentDescription = category.name
                }.also { tab ->
                    binding.tabs.addTab(tab)
                }
            }
            binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    viewModel.onEmojiTabSelected(tab.tag.toString())
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
            viewModel.onEmojiTabSelected(binding.tabs.getTabAt(0)?.tag.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}