package home.visitor;

import home.model.Home;
import home.model.Room;
import home.model.SmokeDetector;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class SmokeDetectorTest {
    @Test void everyVisitorHandlesNewLeafInTree() {
        var detector = new SmokeDetector("Smoke", 2, false);
        var home = new Home("H",List.of(new Room("R",List.of(detector))));
        assertEquals(2,home.accept(new PowerVisitor()));
        assertEquals(35,home.accept(new ReplacementCostVisitor()));
        assertEquals(List.of(detector),home.accept(new SecurityVisitor()));
        var inventory = new InventoryVisitor(); home.accept(inventory);
        assertEquals(Map.of("smokeDetector",1),inventory.counts());
        assertEquals("{\"type\":\"home\",\"name\":\"H\",\"children\":[{\"type\":\"room\",\"name\":\"R\",\"children\":[{\"type\":\"smokeDetector\",\"name\":\"Smoke\",\"watts\":2.0,\"healthy\":false}]}]}",home.accept(new JsonVisitor()));
    }
    @Test void healthyDetectorPassesAuditAndRejectsInvalidPower() {
        assertTrue(new SmokeDetector("S",2,true).accept(new SecurityVisitor()).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new SmokeDetector("S",Double.POSITIVE_INFINITY,true));
    }
}
