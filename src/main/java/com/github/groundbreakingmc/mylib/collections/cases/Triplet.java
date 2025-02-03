package com.github.groundbreakingmc.mylib.collections.cases;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor @Getter @Setter
public class Triplet<L, M, R> {

    private L left;
    private M middle;
    private R right;

    public boolean equals(final Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        final Triplet<?, ?, ?> other = (Triplet<?, ?, ?>) object;
        return this.left.equals(other.left)
                && this.middle.equals(other.middle)
                && this.right.equals(other.right);
    }

    @Override
    public int hashCode() {
        int result = 31 * 17 + (this.left == null ? 0 : this.left.hashCode());
        result = 31 * result + (this.middle == null ? 0 : this.middle.hashCode());
        result = 31 * result + (this.right == null ? 0 : this.right.hashCode());
        return result;
    }
}
