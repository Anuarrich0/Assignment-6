package home.visitor;

import home.model.Camera;
import home.model.DoorLock;
import home.model.Home;
import home.model.Lamp;
import home.model.Room;
import home.model.SmokeDetector;
import home.model.Thermostat;

import java.util.LinkedHashMap;
import java.util.Map;

public final class InventoryVisitor implements Visitor<Void> {
    private final Map<String, Integer> counts = new LinkedHashMap<>();
    public Map<String, Integer> counts() { return Map.copyOf(counts); }
    private Void count(String type) { counts.merge(type, 1, Integer::sum); return null; }
    @Override public Void visit(Home home) { home.children().forEach(r -> r.accept(this)); return null; }
    @Override public Void visit(Room room) { room.children().forEach(e -> e.accept(this)); return null; }
    @Override public Void visit(Lamp lamp) { return count("lamp"); }
    @Override public Void visit(Thermostat thermostat) { return count("thermostat"); }
    @Override public Void visit(Camera camera) { return count("camera"); }
    @Override public Void visit(DoorLock lock) { return count("lock"); }
    @Override public Void visit(SmokeDetector detector) { return count("smokeDetector"); }
}
