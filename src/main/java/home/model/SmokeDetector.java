package home.model;

import home.visitor.Visitor;

import java.util.Objects;

public record SmokeDetector(String name, double watts, boolean healthy) implements Element {
    public SmokeDetector {
        Objects.requireNonNull(name);
        if (name.isBlank() || !Double.isFinite(watts) || watts < 0) throw new IllegalArgumentException("Invalid detector data");
    }
    @Override public <R> R accept(Visitor<R> visitor) { return visitor.visit(this); }
}
