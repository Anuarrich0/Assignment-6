package home.visitor;

import home.elements.Camera;
import home.elements.DoorLock;
import home.elements.Home;
import home.elements.Lamp;
import home.elements.Room;
import home.elements.Thermostat;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReplacementCostTest {
    @Test void calculatesReplacementBudget() {
        var room = new Room("Hall", List.of(new Lamp("L",10,true), new Thermostat("T",100,22), new Camera("C",5,true), new DoorLock("D",1,true)));
        assertEquals(310, new Home("H",List.of(room)).accept(new ReplacementCostVisitor()));
    }
}
