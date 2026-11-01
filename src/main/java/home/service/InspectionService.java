package home.service;

import home.model.Element;
import home.model.Room;
import home.observer.EventSource;
import home.observer.EventType;
import home.observer.HomeEvent;
import home.observer.Observer;
import home.visitor.SecurityVisitor;

import java.util.Map;
import java.util.Objects;

public final class InspectionService implements Observer<HomeEvent> {
    private final Map<String, Room> rooms;
    private final EventSource<HomeEvent> reports;
    public InspectionService(Map<String, Room> rooms, EventSource<HomeEvent> reports) {
        this.rooms = Map.copyOf(rooms); this.reports = Objects.requireNonNull(reports);
    }
    @Override public void onEvent(HomeEvent event) {
        if (event.type() != EventType.MOTION || event.value() <= 0) return;
        Room room = rooms.get(event.room());
        if (room == null) throw new IllegalArgumentException("Unknown room: " + event.room());
        var findings = room.accept(new SecurityVisitor());
        reports.publish(new HomeEvent(EventType.INSPECTION_REPORT, room.name(), "inspection", findings.size(),
                findings.stream().map(Element::name).toList().toString()));
    }
}
