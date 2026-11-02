package home.visitor;

import home.elements.Camera;
import home.elements.DoorLock;
import home.elements.Home;
import home.elements.Lamp;
import home.elements.Room;
import home.elements.SmokeDetector;
import home.elements.Thermostat;

import java.util.stream.Collectors;

public final class JsonVisitor implements Visitor<String> {
    static String quote(String text) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 32 || Character.isSurrogate(c)) out.append(String.format("\\u%04x", (int)c));
                    else out.append(c);
                }
            }
        }
        return out.append('"').toString();
    }
    private String header(String type, String name) { return "{\"type\":" + quote(type) + ",\"name\":" + quote(name); }
    private String leaf(String type, String name, double watts, String state) {
        return header(type, name) + ",\"watts\":" + watts + "," + state + "}";
    }
    @Override public String visit(Home home) {
        return header("home", home.name()) + ",\"children\":[" + home.children().stream().map(r -> r.accept(this)).collect(Collectors.joining(",")) + "]}";
    }
    @Override public String visit(Room room) {
        return header("room", room.name()) + ",\"children\":[" + room.children().stream().map(e -> e.accept(this)).collect(Collectors.joining(",")) + "]}";
    }
    @Override public String visit(Lamp lamp) { return leaf("lamp", lamp.name(), lamp.watts(), "\"on\":" + lamp.on()); }
    @Override public String visit(Thermostat thermostat) { return leaf("thermostat", thermostat.name(), thermostat.watts(), "\"targetCelsius\":" + thermostat.targetCelsius()); }
    @Override public String visit(Camera camera) { return leaf("camera", camera.name(), camera.watts(), "\"recording\":" + camera.recording()); }
    @Override public String visit(DoorLock lock) { return leaf("lock", lock.name(), lock.watts(), "\"locked\":" + lock.locked()); }
    @Override public String visit(SmokeDetector detector) { return leaf("smokeDetector", detector.name(), detector.watts(), "\"healthy\":" + detector.healthy()); }
}
