package com.example.studentexpensetracker.ui.split;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.studentexpensetracker.data.local.entity.split.ExpenseGroupEntity;
import com.example.studentexpensetracker.data.local.entity.split.FriendEntity;
import com.example.studentexpensetracker.data.repository.SplitRepository;

import java.util.List;

public class SplitViewModel extends AndroidViewModel {

    private final SplitRepository repository;
    private final LiveData<List<ExpenseGroupEntity>> allGroups;

    public SplitViewModel(@NonNull Application application) {
        super(application);
        repository = SplitRepository.getInstance(application);
        allGroups = repository.getAllGroups();
    }

    public LiveData<List<ExpenseGroupEntity>> getAllGroups() {
        return allGroups;
    }

    public void createGroup(String name, String description, List<FriendEntity> friends, SplitRepository.Callback<Long> onComplete) {
        repository.createGroupWithMembers(name, description, friends, onComplete);
    }

    public void deleteGroup(ExpenseGroupEntity group) {
        repository.deleteGroup(group);
    }
}
