package home.visitor;

import home.model.Camera;
import home.model.DoorLock;
import home.model.Home;
import home.model.Lamp;
import home.model.Room;
import home.model.SmokeDetector;
import home.model.Thermostat;

public final class PowerVisitor implements Visitor<Double> {
    @Override public Double visit(Home home) { return home.children().stream().mapToDouble(r -> r.accept(this)).sum(); }
    @Override public Double visit(Room room) { return room.children().stream().mapToDouble(e -> e.accept(this)).sum(); }
    @Override public Double visit(Lamp lamp) { return lamp.on() ? lamp.watts() : 0.0; }
    @Override public Double visit(Thermostat thermostat) { return thermostat.watts(); }
    @Override public Double visit(Camera camera) { return camera.recording() ? camera.watts() : 0.0; }
    @Override public Double visit(DoorLock lock) { return lock.watts(); }
    @Override public Double visit(SmokeDetector detector) { return detector.watts(); }
}
