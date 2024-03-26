package org.futo.circles.feature.timeline.list.holder

import android.view.ViewGroup
import org.futo.circles.R
import org.futo.circles.core.base.list.ViewBindingHolder
import org.futo.circles.core.model.PollContent
import org.futo.circles.core.model.Post
import org.futo.circles.databinding.ViewPollPostBinding
import org.futo.circles.feature.timeline.list.PostOptionsListener
import org.futo.circles.view.PostFooterView
import org.futo.circles.view.PostHeaderView
import org.futo.circles.view.PostStatusView
import org.futo.circles.view.ReadMoreTextView

class PollPostViewHolder(
    parent: ViewGroup,
    private val postOptionsListener: PostOptionsListener,
    isThread: Boolean
) : PostViewHolder(inflate(parent, ViewPollPostBinding::inflate), postOptionsListener, isThread) {

    private companion object : ViewBindingHolder

    private val binding = baseBinding as ViewPollPostBinding
    override val postLayout: ViewGroup
        get() = binding.lCard
    override val postHeader: PostHeaderView
        get() = binding.postHeader
    override val postFooter: PostFooterView
        get() = binding.postFooter
    override val postStatus: PostStatusView
        get() = binding.vPostStatus

    override val readMoreTextView: ReadMoreTextView
        get() = binding.pollContentView.findViewById(R.id.tvPollQuestion)

    init {
        setListeners()
    }

    override fun bind(post: Post) {
        super.bind(post)
        val content = (post.content as? PollContent) ?: return
        binding.pollContentView.setup(content) { optionId ->
            postOptionsListener.onPollOptionSelected(post.postInfo.roomId, post.id, optionId)
        }
    }
}