package com.github.groundbreakingmc.mylib.collections.cases;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Getter
public class ImmutableCase<L, R> {
    private final L left;
    private final R right;

    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof ImmutableCase)) {
            return false;
        }

        final ImmutableCase<?, ?> other = (ImmutableCase<?, ?>) object;
        return this.left.equals(other.left)
                && this.right.equals(other.right);
    }
}
