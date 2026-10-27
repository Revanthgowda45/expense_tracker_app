package com.example.studentexpensetracker.domain.split;

public class Debt {
    private final long fromId;
    private final long toId;
    private final double amount;

    public Debt(long fromId, long toId, double amount) {
        this.fromId = fromId;
        this.toId = toId;
        this.amount = amount;
    }

    public long getFromId() {
        return fromId;
    }

    public long getToId() {
        return toId;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Debt)) return false;
        Debt debt = (Debt) o;
        return fromId == debt.fromId && toId == debt.toId && Double.compare(debt.amount, amount) == 0;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(fromId);
        result = 31 * result + Long.hashCode(toId);
        result = 31 * result + Double.hashCode(amount);
        return result;
    }

    @Override
    public String toString() {
        return "Debt{" + "fromId=" + fromId + ", toId=" + toId + ", amount=" + amount + '}';
    }
}
