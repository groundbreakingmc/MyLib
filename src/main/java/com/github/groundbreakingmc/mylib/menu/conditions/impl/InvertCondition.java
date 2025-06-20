package com.github.groundbreakingmc.mylib.menu.conditions.impl;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.menu.actions.contexts.MenuContext;
import com.github.groundbreakingmc.mylib.menu.conditions.MenuCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class InvertCondition implements MenuCondition<MenuContext> {

    private final MenuCondition<MenuContext> condition;

    public InvertCondition(@NotNull MenuCondition<MenuContext> condition) {
        this.condition = condition;
    }

    @Override
    public boolean test(@NotNull MenuContext context) {
        return !this.condition.test(context);
    }

    @Override
    public @NotNull List<Action<MenuContext>> getDenyActions() {
        return this.condition.getDenyActions();
    }

    @Override
    public @NotNull String getName() {
        return this.condition.getName();
    }
}
