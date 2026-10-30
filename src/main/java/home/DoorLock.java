package home;

import java.util.Objects;

public record DoorLock(String name, double watts, boolean locked) implements Element {
    public DoorLock {
        Objects.requireNonNull(name);
        if (name.isBlank() || !Double.isFinite(watts) || watts < 0) throw new IllegalArgumentException("Invalid device data");
        
    }
    @Override public <R> R accept(Visitor<R> visitor) { return visitor.visit(this); }
}
