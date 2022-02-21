package com.futo.circles.ui.groups.timeline

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.futo.circles.R
import com.futo.circles.databinding.GroupTimelineFragmentBinding
import com.futo.circles.extensions.observeData
import com.futo.circles.extensions.setToolbarTitle
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class GroupTimelineFragment : Fragment(R.layout.group_timeline_fragment) {

    private val args: GroupTimelineFragmentArgs by navArgs()
    private val viewModel by viewModel<GroupTimelineViewModel> { parametersOf(args.roomId) }
    private val binding by viewBinding(GroupTimelineFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.titleLiveData.observeData(this) { title -> setToolbarTitle(title) }
    }
}