package home.visitor;

import home.ProducerRunner;
import home.elements.Camera;
import home.elements.DoorLock;
import home.elements.Element;
import home.elements.Home;
import home.elements.Lamp;
import home.elements.Room;
import home.elements.Thermostat;
import home.observer.EventSource;
import home.observer.EventType;
import home.observer.HomeEvent;
import home.observer.Observer;
import home.subscribers.EnergyTracker;
import home.subscribers.EventLog;
import home.subscribers.InspectionService;
import home.subscribers.MobileApp;
import home.subscribers.SecurityAlarm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class HomeTest {
    private final Room room = new Room("Hall", List.of(new Lamp("Lamp", 10, true), new Thermostat("Heat", 100, 22), new Camera("Camera", 5, false), new DoorLock("Door", 1, false)));
    private HomeEvent event(EventType type) { return new HomeEvent(type, "Hall", "Sensor", 1, "sample"); }

    @Test void routesOnlySelectedType() {
        var source = new EventSource<HomeEvent>(); var count = new AtomicInteger();
        source.subscribe(EventType.MOTION, e -> count.incrementAndGet());
        source.publish(event(EventType.DEVICE_STATE)); source.publish(event(EventType.MOTION));
        assertEquals(1, count.get());
    }
    @Test void duplicateSubscriptionIsIgnored() {
        var source = new EventSource<HomeEvent>(); var count = new AtomicInteger();
        Observer<HomeEvent> listener = e -> count.incrementAndGet();
        source.subscribe(EventType.MOTION, listener); source.subscribe(EventType.MOTION, listener);
        source.publish(event(EventType.MOTION)); assertEquals(1, count.get());
    }
    @Test void unsubscribeStopsFutureDelivery() {
        var source = new EventSource<HomeEvent>(); var log = new EventLog();
        source.subscribe(EventType.MOTION, log); source.publish(event(EventType.MOTION));
        source.unsubscribe(EventType.MOTION, log); source.publish(event(EventType.MOTION));
        assertEquals(1, log.events().size());
    }
    @Test void listenerCanLeaveInsideHandler() {
        var source = new EventSource<HomeEvent>(); var count = new AtomicInteger();
        Observer<HomeEvent> listener = new Observer<>() {
            public void onEvent(HomeEvent e) { count.incrementAndGet(); source.unsubscribe(e.type(), this); }
        };
        source.subscribe(EventType.MOTION, listener);
        source.publish(event(EventType.MOTION)); source.publish(event(EventType.MOTION));
        assertEquals(1, count.get());
    }
    @Test void listenerCanJoinInsideHandler() {
        var source = new EventSource<HomeEvent>(); var log = new EventLog();
        source.subscribe(EventType.MOTION, e -> source.subscribe(e.type(), log));
        source.publish(event(EventType.MOTION)); assertTrue(log.events().isEmpty());
        source.publish(event(EventType.MOTION)); assertEquals(1, log.events().size());
    }
    @Test void failedListenerDoesNotSkipOthers() {
        var source = new EventSource<HomeEvent>(); var log = new EventLog();
        source.subscribe(EventType.MOTION, e -> { throw new IllegalArgumentException("broken"); });
        source.subscribe(EventType.MOTION, log);
        var failure = assertThrows(IllegalStateException.class, () -> source.publish(event(EventType.MOTION)));
        assertEquals(1, failure.getSuppressed().length); assertEquals(1, log.events().size());
    }
    @Test void chainPublishesAggregatedEnergy() {
        var energy = new EnergyTracker(new EventSource<>()); var log = new EventLog();
        energy.subscribe(EventType.ENERGY_SUMMARY, log);
        energy.onEvent(event(EventType.DEVICE_STATE)); energy.onEvent(event(EventType.DEVICE_STATE));
        assertEquals(2, energy.total("Hall"));
        assertEquals(List.of(1.0, 2.0), log.events().stream().map(HomeEvent::value).toList());
    }
    @Test void energyIgnoresMotion() {
        var energy = new EnergyTracker(new EventSource<>()); energy.onEvent(event(EventType.MOTION));
        assertEquals(0, energy.total("Hall"));
    }
    @Test void computesHandCalculatedPower() { assertEquals(111.0, room.accept(new PowerVisitor())); }
    @Test void inactiveDevicesConsumeZero() {
        assertEquals(0, new Lamp("Off", 10, false).accept(new PowerVisitor()));
        assertEquals(0, new Camera("Off", 5, false).accept(new PowerVisitor()));
    }
    @Test void homeAddsRoomTotals() {
        var other = new Room("Kitchen", List.of(new Lamp("Light", 20, true)));
        assertEquals(131, new Home("Home", List.of(room, other)).accept(new PowerVisitor()));
    }
    @Test void inventoryAccumulatesAcrossRooms() {
        var visitor = new InventoryVisitor(); new Home("Home", List.of(room, room)).accept(visitor);
        assertEquals(Map.of("lamp",2,"thermostat",2,"camera",2,"lock",2), visitor.counts());
        assertThrows(UnsupportedOperationException.class, () -> visitor.counts().put("x", 1));
    }
    @Test void filtersExactSecurityRisks() {
        assertEquals(List.of("Camera", "Door"), room.accept(new SecurityVisitor()).stream().map(Element::name).toList());
    }
    @Test void safeDevicesHaveNoFindings() {
        var safe = new Room("Safe", List.of(new Camera("Camera", 5, true), new DoorLock("Door",1,true)));
        assertTrue(safe.accept(new SecurityVisitor()).isEmpty());
    }
    @Test void jsonHasExactNesting() {
        var tree = new Home("H", List.of(new Room("R", List.of(new Lamp("L",10,true)))));
        assertEquals("{\"type\":\"home\",\"name\":\"H\",\"children\":[{\"type\":\"room\",\"name\":\"R\",\"children\":[{\"type\":\"lamp\",\"name\":\"L\",\"watts\":10.0,\"on\":true}]}]}",tree.accept(new JsonVisitor()));
    }
    @Test void jsonEscapesSpecialCharacters() {
        assertEquals("\"a\\\"\\\\\\n\\r\\t\\u0001\"", JsonVisitor.quote("a\"\\\n\r\t\u0001"));
        assertEquals("\"\\ud83d\\ude00\"", JsonVisitor.quote("\ud83d\ude00"));
    }
    @Test void integrationInspectsOnlyEventRoom() {
        var reports = new EventSource<HomeEvent>(); var log = new EventLog();
        var other = new Room("Other", List.of(new DoorLock("Other door", 1, false)));
        var service = new InspectionService(Map.of("Hall", room,"Other",other), reports);
        reports.subscribe(EventType.INSPECTION_REPORT, log); service.onEvent(event(EventType.MOTION));
        assertEquals(1, log.events().size()); assertEquals(2,log.events().get(0).value());
        assertEquals("[Camera, Door]",log.events().get(0).detail());
    }
    @Test void unknownRoomIsReportedAsError() {
        var service = new InspectionService(Map.of(), new EventSource<>());
        assertThrows(IllegalArgumentException.class, () -> service.onEvent(event(EventType.MOTION)));
    }
    @Test void compositeCopiesInput() {
        var children = new ArrayList<Element>(); children.add(new Lamp("L",1,true));
        var copied = new Room("R", children); children.clear();
        assertEquals(1,copied.children().size());
        assertThrows(UnsupportedOperationException.class, () -> copied.children().clear());
    }
    @Test void rejectsInvalidData() {
        assertThrows(IllegalArgumentException.class, () -> new Lamp("L", -1,true));
        assertThrows(IllegalArgumentException.class, () -> new Thermostat("T", 1,Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new HomeEvent(EventType.MOTION,"R","D",Double.NaN,""));
        assertThrows(IllegalArgumentException.class, () -> ProducerRunner.run(0,List.of()));
    }
    @Test void alarmAndMobileKeepState() {
        var alarm = new SecurityAlarm(); var mobile = new MobileApp();
        alarm.onEvent(event(EventType.MOTION)); alarm.onEvent(event(EventType.DEVICE_STATE));
        mobile.onEvent(event(EventType.MOTION)); mobile.onEvent(event(EventType.DEVICE_STATE));
        assertEquals(1,alarm.alerts()); assertEquals(EventType.DEVICE_STATE,mobile.latest().get("Hall/Sensor").type());
    }
    @Test @Timeout(15) void fourThreadsDeliverExactlyFourThousandEvents() throws Exception {
        var source = new EventSource<HomeEvent>(); var count = new AtomicInteger();
        var energy = new EnergyTracker(new EventSource<>()); var summaries = new AtomicInteger();
        source.subscribe(EventType.DEVICE_STATE, e -> count.incrementAndGet());
        source.subscribe(EventType.DEVICE_STATE, energy);
        energy.subscribe(EventType.ENERGY_SUMMARY, e -> summaries.incrementAndGet());
        var ready = new CountDownLatch(4); var start = new CountDownLatch(1);
        var producers = new ArrayList<Runnable>();
        for (int t=0;t<4;t++) producers.add(() -> {
            ready.countDown();
            try { ready.await(); start.countDown(); start.await(); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw new IllegalStateException(ex); }
            for (int i=0;i<1000;i++) source.publish(event(EventType.DEVICE_STATE));
        });
        ProducerRunner.run(4, producers);
        assertEquals(4000,count.get()); assertEquals(4000,summaries.get()); assertEquals(4000,energy.total("Hall"));
    }
    @Test @Timeout(10) void concurrentUnsubscribeUsesInFlightSnapshot() throws Exception {
        var source = new EventSource<HomeEvent>(); var log = new EventLog();
        var entered = new CountDownLatch(1); var release = new CountDownLatch(1);
        source.subscribe(EventType.MOTION, e -> {
            entered.countDown();
            try { release.await(); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw new IllegalStateException(ex); }
        });
        source.subscribe(EventType.MOTION,log);
        var executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        try {
            var future = executor.submit(() -> source.publish(event(EventType.MOTION)));
            assertTrue(entered.await(3,java.util.concurrent.TimeUnit.SECONDS));
            source.unsubscribe(EventType.MOTION,log); release.countDown(); future.get();
            source.publish(event(EventType.MOTION)); assertEquals(1,log.events().size());
        } finally { release.countDown(); executor.shutdown(); assertTrue(executor.awaitTermination(3,java.util.concurrent.TimeUnit.SECONDS)); }
    }
    @Test @Timeout(10) void producerFailurePropagatesAfterOtherWorkFinishes() {
        var count = new AtomicInteger();
        assertThrows(java.util.concurrent.ExecutionException.class, () -> ProducerRunner.run(3,List.of(
            () -> { throw new IllegalStateException("producer failed"); },
            () -> { for(int i=0;i<100;i++) count.incrementAndGet(); },
            () -> { for(int i=0;i<100;i++) count.incrementAndGet(); })));
        assertEquals(200,count.get());
    }
}
