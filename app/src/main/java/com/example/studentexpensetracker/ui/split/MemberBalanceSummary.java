package com.example.studentexpensetracker.ui.split;

import com.example.studentexpensetracker.data.local.entity.split.FriendEntity;

public class MemberBalanceSummary {
    private final FriendEntity friend;
    private final double totalPaid;
    private final double totalShare;
    private final double netBalance; // Positive = owed to member, Negative = member owes

    public MemberBalanceSummary(FriendEntity friend, double totalPaid, double totalShare, double netBalance) {
        this.friend = friend;
        this.totalPaid = totalPaid;
        this.totalShare = totalShare;
        this.netBalance = netBalance;
    }

    public FriendEntity getFriend() {
        return friend;
    }

    public double getTotalPaid() {
        return totalPaid;
    }

    public double getTotalShare() {
        return totalShare;
    }

    public double getNetBalance() {
        return netBalance;
    }
}
