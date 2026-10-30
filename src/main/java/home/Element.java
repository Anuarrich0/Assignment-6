package home;

public interface Element {
    String name();
    <R> R accept(Visitor<R> visitor);
}
