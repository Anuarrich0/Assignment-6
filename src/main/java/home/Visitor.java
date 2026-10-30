package home;

public interface Visitor<R> {
    R visit(Home home);
    R visit(Room room);
    R visit(Lamp lamp);
    R visit(Thermostat thermostat);
    R visit(Camera camera);
    R visit(DoorLock lock);
}
