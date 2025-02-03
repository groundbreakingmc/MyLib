package com.github.groundbreakingmc.mylib.collections.cases;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor @Getter @Setter
public class Case<L, R> {
    private L left;
    private R right;

    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof Case)) {
            return false;
        }

        final Case<?, ?> other = (Case<?, ?>) object;
        return this.left.equals(other.left)
                && this.right.equals(other.right);
    }
}
