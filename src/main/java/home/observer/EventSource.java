package home.observer;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;

public final class EventSource<E extends TypedEvent> implements Subject<E> {
    private final EnumMap<EventType, CopyOnWriteArraySet<Observer<E>>> listeners = new EnumMap<>(EventType.class);

    public EventSource() {
        for (EventType type : EventType.values()) listeners.put(type, new CopyOnWriteArraySet<>());
    }
    @Override public void subscribe(EventType type, Observer<E> observer) {
        listeners.get(Objects.requireNonNull(type)).add(Objects.requireNonNull(observer));
    }
    @Override public void unsubscribe(EventType type, Observer<E> observer) {
        listeners.get(Objects.requireNonNull(type)).remove(Objects.requireNonNull(observer));
    }
    public void publish(E event) {
        Objects.requireNonNull(event);
        var wildcard = new LinkedHashSet<>(listeners.get(event.type()));
        wildcard.addAll(listeners.get(EventType.ALL));
        RuntimeException failure = null;
        for (Observer<E> observer : wildcard) {
            try { observer.onEvent(event); }
            catch (RuntimeException ex) {
                if (failure == null) failure = new IllegalStateException("Listener failed");
                failure.addSuppressed(ex);
            }
        }
        if (failure != null) throw failure;
    }
}
// Add a wildcard subscription to ALL event types. A wildcard listener receives every event exactly once,even if also subscribes to one specific type