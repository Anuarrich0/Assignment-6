package home.visitor;

import home.model.Camera;
import home.model.DoorLock;
import home.model.Home;
import home.model.Lamp;
import home.model.Room;
import home.model.Thermostat;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReplacementCostTest {
    @Test void calculatesReplacementBudget() {
        var room = new Room("Hall", List.of(new Lamp("L",10,true), new Thermostat("T",100,22), new Camera("C",5,true), new DoorLock("D",1,true)));
        assertEquals(310, new Home("H",List.of(room)).accept(new ReplacementCostVisitor()));
    }
}
