package home.service;

import home.observer.EventType;
import home.observer.HomeEvent;
import home.observer.Observer;

import java.util.concurrent.atomic.AtomicInteger;

public final class SecurityAlarm implements Observer<HomeEvent> {
    private final AtomicInteger alerts = new AtomicInteger();
    @Override public void onEvent(HomeEvent event) { if (event.type() == EventType.MOTION && event.value() > 0) alerts.incrementAndGet(); }
    public int alerts() { return alerts.get(); }
}
