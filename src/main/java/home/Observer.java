package home;

@FunctionalInterface
public interface Observer<E> { void onEvent(E event); }
