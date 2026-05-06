package com.github.groundbreakingmc.mylib.eventbus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventBusTest {

    private EventBus bus;

    @BeforeEach
    void setUp() {
        this.bus = new EventBus();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    static class SimpleEvent implements Event {}

    static class OtherEvent implements Event {}

    static class CancellableEvent implements Event, Cancellable {

        private boolean cancelled;

        @Override
        public boolean isCancelled() {
            return this.cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }

    // -------------------------------------------------------------------------
    // Basic dispatch
    // -------------------------------------------------------------------------

    @Test
    void handlerIsInvokedOnPost() {
        final List<Event> received = new ArrayList<>();

        this.bus.register(SimpleEvent.class, (listener, event) -> received.add(event), null, Priorities.NORMAL, false);
        this.bus.bake();
        this.bus.post(new SimpleEvent());

        assertEquals(1, received.size());
    }

    @Test
    void handlerIsNotInvokedForDifferentEventType() {
        final List<Event> received = new ArrayList<>();

        this.bus.register(OtherEvent.class, (listener, event) -> received.add(event), null, Priorities.NORMAL, false);
        this.bus.bake();
        this.bus.post(new SimpleEvent());

        assertTrue(received.isEmpty());
    }

    @Test
    void postWithNoHandlersDoesNotThrow() {
        this.bus.bake();
        assertDoesNotThrow(() -> this.bus.post(new SimpleEvent()));
    }

    // -------------------------------------------------------------------------
    // Annotation-based registration
    // -------------------------------------------------------------------------

    @Test
    void annotationHandlerIsInvoked() {
        final List<Event> received = new ArrayList<>();

        this.bus.register(new Object() {
            @Subscribe
            public void on(SimpleEvent event) {
                received.add(event);
            }
        });
        this.bus.bake();
        this.bus.post(new SimpleEvent());

        assertEquals(1, received.size());
    }

    @Test
    void registerThrowsIfMethodHasNoParameters() {
        assertThrows(IllegalArgumentException.class, () ->
                this.bus.register(new Object() {
                    @Subscribe
                    public void on() {
                    }
                })
        );
    }

    @Test
    void registerThrowsIfParameterDoesNotImplementEvent() {
        assertThrows(IllegalArgumentException.class, () ->
                this.bus.register(new Object() {
                    @Subscribe
                    public void on(String s) {
                    }
                })
        );
    }

    // -------------------------------------------------------------------------
    // Priority ordering
    // -------------------------------------------------------------------------

    @Test
    void handlersAreInvokedInPriorityOrder() {
        final List<Integer> order = new ArrayList<>();

        this.bus.register(SimpleEvent.class, (l, e) -> order.add(1), null, Priorities.LOW, false);
        this.bus.register(SimpleEvent.class, (l, e) -> order.add(2), null, Priorities.HIGH, false);
        this.bus.register(SimpleEvent.class, (l, e) -> order.add(3), null, Priorities.NORMAL, false);
        this.bus.bake();
        this.bus.post(new SimpleEvent());

        assertEquals(List.of(2, 3, 1), order);
    }

    @Test
    void lateRegistrationIsInsertedInPriorityOrder() {
        final List<Integer> order = new ArrayList<>();

        this.bus.register(SimpleEvent.class, (l, e) -> order.add(1), null, Priorities.LOW, false);
        this.bus.register(SimpleEvent.class, (l, e) -> order.add(3), null, Priorities.HIGH, false);
        this.bus.bake();

        this.bus.register(SimpleEvent.class, (l, e) -> order.add(2), null, Priorities.NORMAL, false);
        this.bus.post(new SimpleEvent());

        assertEquals(List.of(3, 2, 1), order);
    }

    // -------------------------------------------------------------------------
    // Cancellable
    // -------------------------------------------------------------------------

    @Test
    void ignoreCancelledSkipsHandlerWhenCancelled() {
        final List<String> invoked = new ArrayList<>();

        this.bus.register(CancellableEvent.class, (l, e) -> {
            ((Cancellable) e).setCancelled(true);
            invoked.add("first");
        }, null, Priorities.HIGH, false);

        this.bus.register(CancellableEvent.class, (l, e) -> invoked.add("second"), null, Priorities.NORMAL, true);
        this.bus.bake();
        this.bus.post(new CancellableEvent());

        assertEquals(List.of("first"), invoked);
    }

    @Test
    void handlerWithIgnoreCancelledFalseRunsEvenWhenCancelled() {
        final List<String> invoked = new ArrayList<>();

        this.bus.register(CancellableEvent.class, (l, e) -> {
            ((Cancellable) e).setCancelled(true);
            invoked.add("first");
        }, null, Priorities.HIGH, false);

        this.bus.register(CancellableEvent.class, (l, e) -> invoked.add("second"), null, Priorities.NORMAL, false);
        this.bus.bake();
        this.bus.post(new CancellableEvent());

        assertEquals(List.of("first", "second"), invoked);
    }

    // -------------------------------------------------------------------------
    // unregister(Object)
    // -------------------------------------------------------------------------

    @Test
    void unregisterBeforeBakeRemovesAllHandlersOfListener() {
        final List<Event> received = new ArrayList<>();

        final Object listener = new Object() {
            @Subscribe
            public void on(SimpleEvent event) {
                received.add(event);
            }
        };

        this.bus.register(listener);
        this.bus.unregister(listener);
        this.bus.bake();
        this.bus.post(new SimpleEvent());

        assertTrue(received.isEmpty());
    }

    @Test
    void unregisterAfterBakeRemovesAllHandlersOfListener() {
        final List<Event> received = new ArrayList<>();

        final Object listener = new Object() {
            @Subscribe
            public void on(SimpleEvent event) {
                received.add(event);
            }
        };

        this.bus.register(listener);
        this.bus.bake();
        this.bus.unregister(listener);
        this.bus.post(new SimpleEvent());

        assertTrue(received.isEmpty());
    }

    @Test
    void unregisterDoesNotAffectOtherListeners() {
        final List<Event> received = new ArrayList<>();

        final Object toRemove = new Object() {
            @Subscribe
            public void on(SimpleEvent event) {
            }
        };

        final Object toKeep = new Object() {
            @Subscribe
            public void on(SimpleEvent event) {
                received.add(event);
            }
        };

        this.bus.register(toRemove);
        this.bus.register(toKeep);
        this.bus.bake();
        this.bus.unregister(toRemove);
        this.bus.post(new SimpleEvent());

        assertEquals(1, received.size());
    }

    // -------------------------------------------------------------------------
    // on() / Registration
    // -------------------------------------------------------------------------

    @Test
    void onHandlerIsInvoked() {
        final List<Event> received = new ArrayList<>();

        this.bus.on(SimpleEvent.class, (l, e) -> received.add(e));
        this.bus.bake();
        this.bus.post(new SimpleEvent());

        assertEquals(1, received.size());
    }

    @Test
    void onHandlerWithPriorityIsInvokedInOrder() {
        final List<Integer> order = new ArrayList<>();

        this.bus.on(SimpleEvent.class, (l, e) -> order.add(1), Priorities.LOW);
        this.bus.on(SimpleEvent.class, (l, e) -> order.add(2), Priorities.HIGH);
        this.bus.bake();
        this.bus.post(new SimpleEvent());

        assertEquals(List.of(2, 1), order);
    }

    @Test
    void registrationCancelBeforeBakeRemovesHandler() {
        final List<Event> received = new ArrayList<>();

        final ListenerRegistration reg = this.bus.on(SimpleEvent.class, (l, e) -> received.add(e));
        reg.cancel();
        this.bus.bake();
        this.bus.post(new SimpleEvent());

        assertTrue(received.isEmpty());
    }

    @Test
    void registrationCancelAfterBakeRemovesHandler() {
        final List<Event> received = new ArrayList<>();

        final ListenerRegistration reg = this.bus.on(SimpleEvent.class, (l, e) -> received.add(e));
        this.bus.bake();
        reg.cancel();
        this.bus.post(new SimpleEvent());

        assertTrue(received.isEmpty());
    }

    @Test
    void registrationCancelDoesNotAffectOtherHandlers() {
        final List<Event> received = new ArrayList<>();

        final ListenerRegistration reg = this.bus.on(SimpleEvent.class, (l, e) -> {});
        this.bus.on(SimpleEvent.class, (l, e) -> received.add(e));
        this.bus.bake();
        reg.cancel();
        this.bus.post(new SimpleEvent());

        assertEquals(1, received.size());
    }

    @Test
    void registrationCancelCalledTwiceDoesNotThrow() {
        final ListenerRegistration reg = this.bus.on(SimpleEvent.class, (l, e) -> {});
        this.bus.bake();
        reg.cancel();
        assertDoesNotThrow(reg::cancel);
    }

    // -------------------------------------------------------------------------
    // bake() idempotency
    // -------------------------------------------------------------------------

    @Test
    void bakeCalledTwiceDoesNotThrow() {
        this.bus.register(SimpleEvent.class, (l, e) -> {}, null, Priorities.NORMAL, false);
        this.bus.bake();
        assertDoesNotThrow(() -> this.bus.bake());
    }
}
