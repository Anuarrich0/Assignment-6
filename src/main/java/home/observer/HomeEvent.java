package home.observer;

import home.model.Room;

import java.util.Objects;

public record HomeEvent(EventType type, String room, String device, double value, String detail)
        implements TypedEvent {
    public HomeEvent {
        Objects.requireNonNull(type); Objects.requireNonNull(room);
        Objects.requireNonNull(device); Objects.requireNonNull(detail);
        if (room.isBlank() || device.isBlank() || !Double.isFinite(value))
            throw new IllegalArgumentException("Room, device and finite value are required");
    }
}
