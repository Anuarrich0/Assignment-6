package home.visitor;

import home.model.Camera;
import home.model.DoorLock;
import home.model.Home;
import home.model.Lamp;
import home.model.Room;
import home.model.SmokeDetector;
import home.model.Thermostat;

public interface Visitor<R> {
    R visit(Home home);
    R visit(Room room);
    R visit(Lamp lamp);
    R visit(Thermostat thermostat);
    R visit(Camera camera);
    R visit(DoorLock lock);
    R visit(SmokeDetector detector);
}
