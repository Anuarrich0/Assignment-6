package home.model;

import home.visitor.Visitor;

import java.util.Objects;

public record Lamp(String name, double watts, boolean on) implements Element {
    public Lamp {
        Objects.requireNonNull(name);
        if (name.isBlank() || !Double.isFinite(watts) || watts < 0) throw new IllegalArgumentException("Invalid device data");
        
    }
    @Override public <R> R accept(Visitor<R> visitor) { return visitor.visit(this); }
}
