package home;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class EventLog implements Observer<HomeEvent> {
    private final ConcurrentLinkedQueue<HomeEvent> events = new ConcurrentLinkedQueue<>();
    @Override public void onEvent(HomeEvent event) { events.add(event); }
    public List<HomeEvent> events() { return List.copyOf(events); }
}
