package home.elements;

import home.visitor.Visitor;

public interface Element {
    String name();
    <R> R accept(Visitor<R> visitor);
}
