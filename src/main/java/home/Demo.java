package home;

import java.util.List;
import java.util.Map;

public final class Demo {
    private Demo() { }
    public static void main(String[] args) throws Exception {
        int threads = args.length == 0 ? 3 : Integer.parseInt(args[0]);
        var living = new Room("Living room", List.of(new Lamp("Ceiling", 12, true), new Thermostat("Heating", 1000, 22), new Camera("Indoor", 5, false), new DoorLock("Front door", 1, false)));
        var bedroom = new Room("Bedroom", List.of(new Lamp("Bedside", 8, false), new DoorLock("Balcony", 1, true)));
        var home = new Home("My home", List.of(living, bedroom));
        var devices = new EventSource<HomeEvent>();
        var motion = new EventSource<HomeEvent>();
        var reports = new EventSource<HomeEvent>();
        var energy = new EnergyTracker(new EventSource<>());
        var mobile = new MobileApp();
        var alarm = new SecurityAlarm();
        var log = new EventLog();
        var inspection = new InspectionService(Map.of(living.name(), living, bedroom.name(), bedroom), reports);
        devices.subscribe(EventType.DEVICE_STATE, mobile);
        devices.subscribe(EventType.DEVICE_STATE, energy);
        devices.subscribe(EventType.DEVICE_STATE, log);
        motion.subscribe(EventType.MOTION, mobile);
        motion.subscribe(EventType.MOTION, alarm);
        motion.subscribe(EventType.MOTION, inspection);
        motion.subscribe(EventType.MOTION, log);
        energy.subscribe(EventType.ENERGY_SUMMARY, log);
        reports.subscribe(EventType.INSPECTION_REPORT, log);
        ProducerRunner.run(threads, List.of(
            () -> { for (int i = 0; i < 100; i++) devices.publish(new HomeEvent(EventType.DEVICE_STATE, living.name(), "Heating", 2, "Energy sample")); },
            () -> { for (int i = 0; i < 100; i++) devices.publish(new HomeEvent(EventType.DEVICE_STATE, bedroom.name(), "Bedside", 1, "Energy sample")); },
            () -> { for (int i = 0; i < 100; i++) motion.publish(new HomeEvent(EventType.MOTION, living.name(), "Motion sensor", 1, "Movement")); }
        ));
        var inventory = new InventoryVisitor(); home.accept(inventory);
        System.out.println("Power: " + home.accept(new PowerVisitor()) + " W");
        System.out.println("Inventory: " + inventory.counts());
        System.out.println("Security findings: " + home.accept(new SecurityVisitor()).stream().map(Element::name).toList());
        System.out.println("Energy: " + energy.total(living.name()) + " + " + energy.total(bedroom.name()) + " Wh");
        System.out.println("Alerts: " + alarm.alerts() + "; logged events: " + log.events().size());
        System.out.println(home.accept(new JsonVisitor()));
    }
}
