package com.github.groundbreakingmc.mylib.collections.cases;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Getter
public class ImmutableCase<L, R> {
    private final L left;
    private final R right;
}
