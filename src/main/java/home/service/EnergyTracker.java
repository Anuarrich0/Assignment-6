package home.service;

import home.observer.EventSource;
import home.observer.EventType;
import home.observer.HomeEvent;
import home.observer.Observer;
import home.observer.Subject;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class EnergyTracker implements Observer<HomeEvent>, Subject<HomeEvent> {
    private final EventSource<HomeEvent> summaries;
    private final ConcurrentMap<String, Double> totals = new ConcurrentHashMap<>();
    public EnergyTracker(EventSource<HomeEvent> summaries) { this.summaries = Objects.requireNonNull(summaries); }
    @Override public void subscribe(EventType type, Observer<HomeEvent> observer) { summaries.subscribe(type, observer); }
    @Override public void unsubscribe(EventType type, Observer<HomeEvent> observer) { summaries.unsubscribe(type, observer); }
    @Override public void onEvent(HomeEvent event) {
        if (event.type() != EventType.DEVICE_STATE) return;
        double total = totals.merge(event.room(), event.value(), Double::sum);
        summaries.publish(new HomeEvent(EventType.ENERGY_SUMMARY, event.room(), "meter", total, "Cumulative Wh"));
    }
    public double total(String room) { return totals.getOrDefault(room, 0.0); }
}
