package home.subscribers;

import home.observer.HomeEvent;
import home.observer.Observer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class MobileApp implements Observer<HomeEvent> {
    private final ConcurrentMap<String, HomeEvent> latest = new ConcurrentHashMap<>();
    @Override public void onEvent(HomeEvent event) { latest.put(event.room() + "/" + event.device(), event); }
    public Map<String, HomeEvent> latest() { return Map.copyOf(latest); }
}
