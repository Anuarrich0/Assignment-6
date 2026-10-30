package home;

import java.util.ArrayList;
import java.util.List;

public final class SecurityVisitor implements Visitor<List<Element>> {
    @Override public List<Element> visit(Home home) {
        List<Element> result = new ArrayList<>();
        home.children().forEach(r -> result.addAll(r.accept(this)));
        return List.copyOf(result);
    }
    @Override public List<Element> visit(Room room) {
        List<Element> result = new ArrayList<>();
        room.children().forEach(e -> result.addAll(e.accept(this)));
        return List.copyOf(result);
    }
    @Override public List<Element> visit(Lamp lamp) { return List.of(); }
    @Override public List<Element> visit(Thermostat thermostat) { return List.of(); }
    @Override public List<Element> visit(Camera camera) { return camera.recording() ? List.of() : List.of(camera); }
    @Override public List<Element> visit(DoorLock lock) { return lock.locked() ? List.of() : List.of(lock); }
    @Override public List<Element> visit(SmokeDetector detector) { return detector.healthy() ? List.of() : List.of(detector); }
}
