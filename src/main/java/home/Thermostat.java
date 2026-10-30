package home;

import java.util.Objects;

public record Thermostat(String name, double watts, double targetCelsius) implements Element {
    public Thermostat {
        Objects.requireNonNull(name);
        if (name.isBlank() || !Double.isFinite(watts) || watts < 0) throw new IllegalArgumentException("Invalid device data");
        if (!Double.isFinite(targetCelsius)) throw new IllegalArgumentException("Invalid temperature");
    }
    @Override public <R> R accept(Visitor<R> visitor) { return visitor.visit(this); }
}
