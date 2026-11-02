package home.elements;

import home.visitor.Visitor;

import java.util.List;
import java.util.Objects;

public record Room(String name, List<Element> children) implements Element {
    public Room {
        Objects.requireNonNull(name);
        if (name.isBlank()) throw new IllegalArgumentException("Name is required");
        children = List.copyOf(children);
    }
    @Override public <R> R accept(Visitor<R> visitor) { return visitor.visit(this); }
}
