package com.github.groundbreakingmc.mylib.collections.cases;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor @Getter @Setter
public class Case<L, R> {
    private L left;
    private R right;
}
