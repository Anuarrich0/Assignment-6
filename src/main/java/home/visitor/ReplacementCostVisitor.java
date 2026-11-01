package home.visitor;

import home.model.Camera;
import home.model.DoorLock;
import home.model.Home;
import home.model.Lamp;
import home.model.Room;
import home.model.SmokeDetector;
import home.model.Thermostat;

public final class ReplacementCostVisitor implements Visitor<Integer> {
    @Override public Integer visit(Home home) { return home.children().stream().mapToInt(r -> r.accept(this)).sum(); }
    @Override public Integer visit(Room room) { return room.children().stream().mapToInt(e -> e.accept(this)).sum(); }
    @Override public Integer visit(Lamp lamp) { return 20; }
    @Override public Integer visit(Thermostat thermostat) { return 150; }
    @Override public Integer visit(Camera camera) { return 80; }
    @Override public Integer visit(DoorLock lock) { return 60; }
    @Override public Integer visit(SmokeDetector detector) { return 35; }
}
