package home.visitor;

import home.elements.Camera;
import home.elements.DoorLock;
import home.elements.Home;
import home.elements.Lamp;
import home.elements.Room;
import home.elements.SmokeDetector;
import home.elements.Thermostat;

public final class ReplacementCostVisitor implements Visitor<Integer> {
    @Override public Integer visit(Home home) { return home.children().stream().mapToInt(r -> r.accept(this)).sum(); }
    @Override public Integer visit(Room room) { return room.children().stream().mapToInt(e -> e.accept(this)).sum(); }
    @Override public Integer visit(Lamp lamp) { return 20; }
    @Override public Integer visit(Thermostat thermostat) { return 150; }
    @Override public Integer visit(Camera camera) { return 80; }
    @Override public Integer visit(DoorLock lock) { return 60; }
    @Override public Integer visit(SmokeDetector detector) { return 35; }
}
