package com.example.studentexpensetracker.ui.split;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.example.studentexpensetracker.data.local.entity.split.ExpenseGroupEntity;
import com.example.studentexpensetracker.data.local.entity.split.ExpenseShareEntity;
import com.example.studentexpensetracker.data.local.entity.split.FriendEntity;
import com.example.studentexpensetracker.data.local.entity.split.SettlementEntity;
import com.example.studentexpensetracker.data.local.entity.split.SharedExpenseEntity;
import com.example.studentexpensetracker.data.repository.SplitRepository;
import com.example.studentexpensetracker.domain.split.Debt;
import com.example.studentexpensetracker.domain.split.DebtSimplifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupDetailViewModel extends AndroidViewModel {

    private final SplitRepository repository;
    private long groupId = -1L;

    private LiveData<ExpenseGroupEntity> group;
    private LiveData<List<FriendEntity>> groupMembers;
    private LiveData<List<SharedExpenseEntity>> groupExpenses;
    private LiveData<List<SettlementEntity>> settlements;
    private LiveData<List<ExpenseShareEntity>> allShares;

    private final MediatorLiveData<Double> totalGroupSpending = new MediatorLiveData<>();
    private final MediatorLiveData<List<Debt>> simplifiedDebts = new MediatorLiveData<>();
    private final MediatorLiveData<List<MemberBalanceSummary>> memberSummaries = new MediatorLiveData<>();

    public GroupDetailViewModel(@NonNull Application application) {
        super(application);
        repository = SplitRepository.getInstance(application);
    }

    public void init(long groupId) {
        this.groupId = groupId;
        group = repository.getGroupById(groupId);
        groupMembers = repository.getGroupMembers(groupId);
        groupExpenses = repository.getExpensesForGroup(groupId);
        settlements = repository.getSettlementsForGroupFlow(groupId);
        allShares = repository.getAllSharesForGroupFlow(groupId);

        totalGroupSpending.addSource(groupExpenses, expenses -> {
            double sum = 0.0;
            if (expenses != null) {
                for (SharedExpenseEntity e : expenses) sum += e.getAmount();
            }
            totalGroupSpending.setValue(sum);
        });

        simplifiedDebts.addSource(groupExpenses, exp -> recalculateDebtsAndSummaries());
        simplifiedDebts.addSource(allShares, shares -> recalculateDebtsAndSummaries());
        simplifiedDebts.addSource(settlements, s -> recalculateDebtsAndSummaries());

        memberSummaries.addSource(groupMembers, m -> recalculateDebtsAndSummaries());
        memberSummaries.addSource(groupExpenses, exp -> recalculateDebtsAndSummaries());
        memberSummaries.addSource(allShares, shares -> recalculateDebtsAndSummaries());
        memberSummaries.addSource(settlements, s -> recalculateDebtsAndSummaries());
    }

    private void recalculateDebtsAndSummaries() {
        List<SharedExpenseEntity> expList = groupExpenses != null ? groupExpenses.getValue() : null;
        List<ExpenseShareEntity> shareList = allShares != null ? allShares.getValue() : null;
        List<SettlementEntity> settleList = settlements != null ? settlements.getValue() : null;
        List<FriendEntity> memberList = groupMembers != null ? groupMembers.getValue() : null;

        simplifiedDebts.setValue(DebtSimplifier.simplifyDebts(expList, shareList, settleList));

        if (memberList == null || memberList.isEmpty()) {
            memberSummaries.setValue(Collections.emptyList());
            return;
        }

        List<MemberBalanceSummary> summaries = new ArrayList<>();
        for (FriendEntity member : memberList) {
            double paid = 0.0;
            if (expList != null) {
                for (SharedExpenseEntity e : expList) {
                    if (e.getPaidByFriendId() == member.getFriendId()) {
                        paid += e.getAmount();
                    }
                }
            }

            double share = 0.0;
            if (shareList != null) {
                for (ExpenseShareEntity s : shareList) {
                    if (s.getFriendId() == member.getFriendId()) {
                        share += s.getOwedAmount();
                    }
                }
            }

            double settlementsReceived = 0.0;
            double settlementsPaid = 0.0;
            if (settleList != null) {
                for (SettlementEntity s : settleList) {
                    if (s.getToFriendId() == member.getFriendId()) {
                        settlementsReceived += s.getAmount();
                    }
                    if (s.getFromFriendId() == member.getFriendId()) {
                        settlementsPaid += s.getAmount();
                    }
                }
            }

            double net = Math.round(((paid - share) + (settlementsPaid - settlementsReceived)) * 100.0) / 100.0;
            summaries.add(new MemberBalanceSummary(
                    member,
                    Math.round(paid * 100.0) / 100.0,
                    Math.round(share * 100.0) / 100.0,
                    net
            ));
        }
        memberSummaries.setValue(summaries);
    }

    public LiveData<ExpenseGroupEntity> getGroup() {
        return group;
    }

    public LiveData<List<FriendEntity>> getGroupMembers() {
        return groupMembers;
    }

    public LiveData<List<SharedExpenseEntity>> getGroupExpenses() {
        return groupExpenses;
    }

    public LiveData<Double> getTotalGroupSpending() {
        return totalGroupSpending;
    }

    public LiveData<List<Debt>> getSimplifiedDebts() {
        return simplifiedDebts;
    }

    public LiveData<List<MemberBalanceSummary>> getMemberSummaries() {
        return memberSummaries;
    }

    public void addFriendToGroup(String name, String phoneNumber) {
        if (name == null || name.trim().isEmpty()) return;
        repository.addFriendToExistingGroup(groupId, name.trim(), phoneNumber != null ? phoneNumber.trim() : null);
    }

    public void addSharedExpense(
            String description,
            double totalAmount,
            long paidByFriendId,
            List<ExpenseShareEntity> shares,
            boolean addUserShareToPersonal) {
        if (shares == null || shares.isEmpty() || totalAmount <= 0) return;

        double roundedTotal = Math.round(totalAmount * 100.0) / 100.0;
        SharedExpenseEntity expense = new SharedExpenseEntity(
                groupId,
                description != null && !description.trim().isEmpty() ? description.trim() : "Shared Expense",
                roundedTotal,
                paidByFriendId,
                System.currentTimeMillis()
        );

        double userShare = 0.0;
        List<FriendEntity> members = groupMembers != null ? groupMembers.getValue() : null;
        if (members != null) {
            FriendEntity you = null;
            for (FriendEntity f : members) {
                if ("You".equalsIgnoreCase(f.getName())) {
                    you = f;
                    break;
                }
            }
            if (you != null) {
                for (ExpenseShareEntity s : shares) {
                    if (s.getFriendId() == you.getFriendId()) {
                        userShare = s.getOwedAmount();
                        break;
                    }
                }
            }
        }

        String groupName = group != null && group.getValue() != null ? group.getValue().getName() : "Group";
        repository.addSharedExpense(expense, shares, addUserShareToPersonal, userShare, groupName);
    }

    public void deleteSharedExpense(SharedExpenseEntity expense) {
        repository.deleteSharedExpense(expense);
    }

    public void settleDebt(long fromId, long toId, double amount) {
        if (amount <= 0) return;
        SettlementEntity settlement = new SettlementEntity(
                groupId,
                fromId,
                toId,
                Math.round(amount * 100.0) / 100.0,
                System.currentTimeMillis()
        );
        repository.addSettlement(settlement);
    }
}
