package org.futo.circles.model

import org.futo.circles.R
import org.futo.circles.core.list.IdEntity

sealed class PeopleListItem : IdEntity<String>

data class PeopleHeaderItem(
    val titleRes: Int
) : PeopleListItem() {
    override val id: String = titleRes.toString()

    companion object {
        val knownUsersHeader = PeopleHeaderItem(R.string.known_users)
        val ignoredUsers = PeopleHeaderItem(R.string.ignored_users)
    }
}

data class PeopleUserListItem(
    val user: CirclesUserSummary,
    val isIgnored: Boolean = false
) : PeopleListItem() {
    override val id: String = user.id
}