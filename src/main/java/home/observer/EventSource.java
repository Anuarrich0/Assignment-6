package home.observer;

import java.util.EnumMap;
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
        RuntimeException failure = null;
        for (Observer<E> observer : listeners.get(event.type())) {
            try { observer.onEvent(event); }
            catch (RuntimeException ex) {
                if (failure == null) failure = new IllegalStateException("Listener failed");
                failure.addSuppressed(ex);
            }
        }
        if (failure != null) throw failure;
    }
}
