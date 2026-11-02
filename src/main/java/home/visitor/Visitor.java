package home.visitor;

import home.elements.Camera;
import home.elements.DoorLock;
import home.elements.Home;
import home.elements.Lamp;
import home.elements.Room;
import home.elements.SmokeDetector;
import home.elements.Thermostat;

public interface Visitor<R> {
    R visit(Home home);
    R visit(Room room);
    R visit(Lamp lamp);
    R visit(Thermostat thermostat);
    R visit(Camera camera);
    R visit(DoorLock lock);
    R visit(SmokeDetector detector);
}
