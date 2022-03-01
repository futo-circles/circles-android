package com.futo.circles.feature.group_invite.list

import android.view.ViewGroup
import com.futo.circles.base.BaseRvAdapter
import com.futo.circles.model.HeaderItem
import com.futo.circles.model.InviteMemberListItem
import com.futo.circles.model.NoResultsItem
import com.futo.circles.model.CirclesUser

private enum class InviteListViewType { Header, User, NoResults }

class InviteMembersListAdapter(
    private val onUserSelected: (CirclesUser) -> Unit
) : BaseRvAdapter<InviteMemberListItem, InviteMemberViewHolder>(
    DefaultIdEntityCallback()
) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is HeaderItem -> InviteListViewType.Header.ordinal
        is CirclesUser -> InviteListViewType.User.ordinal
        is NoResultsItem -> InviteListViewType.NoResults.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteMemberViewHolder {
        return when (InviteListViewType.values()[viewType]) {
            InviteListViewType.Header -> HeaderViewHolder(parent)
            InviteListViewType.User -> UserViewHolder(
                parent,
                onMemberClicked = { position -> onUserSelected(getItem(position) as CirclesUser) })
            InviteListViewType.NoResults -> NoResultViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: InviteMemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}