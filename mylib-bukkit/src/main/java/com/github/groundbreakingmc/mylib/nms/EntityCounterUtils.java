package com.github.groundbreakingmc.mylib.nms;

import com.github.groundbreakingmc.mylib.reflect.HandleLookup;
import com.github.groundbreakingmc.mylib.server.version.ServerVersion;
import com.github.groundbreakingmc.mylib.server.version.ServerVersionUtils;

import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;

public final class EntityCounterUtils {

    private static final IntSupplier NEXT_ID;

    private EntityCounterUtils() {
        throw new UnsupportedOperationException();
    }

    public static int nextId() {
        return NEXT_ID.getAsInt();
    }

    static {
        final HandleLookup lookup = HandleLookup.of(NmsClasses.entity());

        if (ServerVersionUtils.isHigherOrEqual(ServerVersion.V1_14_R1)) {
            final String fieldName = ServerVersionUtils.isHigherOrEqual(ServerVersion.V1_17_R1)
                    ? "ENTITY_COUNTER" : "entityCount";
            final AtomicInteger counter = lookup.staticFieldAsSupplier(fieldName, AtomicInteger.class).get();
            NEXT_ID = counter::incrementAndGet;
        } else {
            final VarHandle varHandle = lookup.staticVarHandle("entityCount", int.class);
            NEXT_ID = () -> {
                int current, next;
                do {
                    current = (int) varHandle.getVolatile();
                    next = current + 1;
                } while (!varHandle.compareAndSet(current, next));
                return next;
            };
        }
    }
}
