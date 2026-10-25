package com.example.studentexpensetracker.ui.split

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentexpensetracker.data.local.entity.split.ExpenseGroupEntity
import com.example.studentexpensetracker.data.local.entity.split.FriendEntity
import com.example.studentexpensetracker.data.repository.SplitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplitViewModel @Inject constructor(
    private val splitRepository: SplitRepository
) : ViewModel() {

    val allGroups: StateFlow<List<ExpenseGroupEntity>> = splitRepository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createGroup(name: String, description: String?, friends: List<FriendEntity>) {
        viewModelScope.launch {
            splitRepository.createGroupWithMembers(name, description, friends)
        }
    }
    
    fun updateGroup(group: ExpenseGroupEntity) {
        viewModelScope.launch {
            splitRepository.updateGroup(group)
        }
    }
    
    fun deleteGroup(group: ExpenseGroupEntity) {
        viewModelScope.launch {
            splitRepository.deleteGroup(group)
        }
    }
}
