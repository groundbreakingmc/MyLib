package com.github.groundbreakingmc.mylib.collections.cases;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor @Getter @Setter
public class Pair<L, R> {
    private L left;
    private R right;

    public boolean equals(final Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        final Pair<?, ?> other = (Pair<?, ?>) object;
        return this.left.equals(other.left)
                && this.right.equals(other.right);
    }

    @Override
    public int hashCode() {
        int result = 31 * 17 + (this.left == null ? 0 : this.left.hashCode());
        result = 31 * result + (this.right == null ? 0 : this.right.hashCode());
        return result;
    }
}
