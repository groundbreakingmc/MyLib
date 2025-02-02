package com.github.groundbreakingmc.mylib.command.executors;

import com.github.groundbreakingmc.mylib.command.allowedexecutor.ExecutorCheck;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

public abstract class ModernArgumentExecutor extends ModernExecutor {

    ModernArgumentExecutor(
            final String permission, final String permissionMessage,
            final ExecutorCheck executorCheck, final String executorCheckMessage,
            final Map<String, ModernArgumentExecutor> arguments, final String argumentMessage) {
        super(permission, permissionMessage, executorCheck, executorCheckMessage, arguments, argumentMessage);
    }

    public static ModernArgumentBuilder builder() {
        return new ModernArgumentBuilder();
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ModernArgumentBuilder {
        private String permission;
        private String permissionMessage;
        private ExecutorCheck executorCheck;
        private String executorCheckMessage;
        private String argumentMessage;

        private final Map<String, ModernArgumentExecutor> arguments = new HashMap<>();

        public ModernArgumentBuilder setPermission(final String permission) {
            this.permission = permission;
            return this;
        }

        public ModernArgumentBuilder setPermissionMessage(final String permissionMessage) {
            this.permissionMessage = permissionMessage;
            return this;
        }

        public ModernArgumentBuilder setExecutorCheck(final ExecutorCheck executorCheck) {
            this.executorCheck = executorCheck;
            return this;
        }

        public ModernArgumentBuilder setExecutorCheckMessage(final String executorCheckMessage) {
            this.executorCheckMessage = executorCheckMessage;
            return this;
        }

        public ModernArgumentBuilder setArgumentMessage(final String argumentMessage) {
            this.argumentMessage = argumentMessage;
            return this;
        }

        public ModernArgumentBuilder addArgument(final String argument, final ModernArgumentExecutor argumentExecutor) {
            this.arguments.put(argument, argumentExecutor);
            return this;
        }

        public ModernArgumentExecutor build() {
            return null; // TODO new ModernArgumentExecutor(
//                    this.permission, this.permissionMessage,
//                    this.executorCheck, this.executorCheckMessage,
//                    this.arguments, this.argumentMessage
//            );
        }
    }
}
