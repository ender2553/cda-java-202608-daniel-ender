# Java OOP Secure - Day 1 - Exercise: Extending the Vehicle Simulator

**Builds on:** `Classes AndO bjects`, `Encapsulation`, `Inheritance`, `Composition`, `ListsDemo`, `MapsDemo`, `collections`, `Secure Coding Principles`.

---

## Learning objectives

By the end of this exercise you will have:

- Added a new class to an existing inheritance hierarchy
- Used three different collection types inside one real application, not just a standalone demo
- Found and fixed a security-relevant bug in someone else's code, using only today's concepts
- Practiced reading and extending a multi-file codebase instead of a single-file demo

---

## Part 1 - Extend the Vehicle Hierarchy 

Open `Vehicle.java`, `Car.java`, `Motorcycle.java`, and `Truck.java` and use them as your template.

1. Add a new `Vehicle` subclass called `Bus`.
   - Give it a `passengerCapacity` field (`int`), in addition to everything `Vehicle` already provides.
   - Override `honk()` with your own message.
   - Override `milesPerGallon()` with a reasonable value.
   - Add a `getPassengerCapacity()` getter.
2. Update `VehicleSimulator.addVehicle()` so `Bus` is a fourth menu option.
3. Run the simulator and confirm:
   - You can add a `Bus`.
   - It honks correctly when selected from the fleet.
   - It shows up correctly in `viewFleet()` - override `toString()` the way `Truck` does if you want `passengerCapacity` to display.

**Checkpoint:** compile and run the whole simulator (`javac *.java && java VehicleSimulator`) before moving on.

---

## Part 2 - Bring Collections Into the Simulator 

Right now the simulator only uses a `List<Vehicle>`. Today you'll add three more collection types as real, working menu features - not standalone demos.

**1. `Set<String>` of unique makes**
- Add a menu option: "View makes in the fleet."
- Build a `Set<String>` from the current fleet's makes (loop over `fleet`, call `.add(vehicle.getMake())` on the set for each one).
- Print the set and confirm duplicate makes only show once.

**2. `Map<String, Integer>` of make → vehicle count**
- Add a menu option: "View inventory by make."
- Build the map the way `MapsDemo.java` did: for each vehicle, `map.put(make, map.getOrDefault(make, 0) + 1)`.
- Print it as `"<make>: <count> vehicle(s)"` per line.

**3. `Queue<Vehicle>` for a service line**
- Add two menu options: "Send a vehicle to the service line" and "Service the next vehicle in line."
- Maintain a `Queue<Vehicle> serviceLine` field alongside `fleet`.
- "Send to service line" should `offer()` a selected vehicle onto the queue.
- "Service the next vehicle" should `poll()` it off and print a message confirming which vehicle was serviced.

**Checkpoint:** add two vehicles from the same make, then confirm your `Set` shows one entry for that make and your `Map` shows a count of 2 or more.

---

## Part 3 - Find the Bug (Security Preview) 

This is deliberately similar to `SecureCodingPrinciplesDemo.java` from this morning - same underlying ideas (aliasing, leaked mutable state, defensive copying), a new class, and no answer key until you've tried it yourself.

Below is a `Driver` class for tracking who's licensed to drive vehicles in the fleet. It compiles and runs, but it has vulnerabilities a security reviewer would flag immediately — all of which trace straight back to this morning's `private fields → Encapsulation`, `Getters → Leaked mutable state`, and `Constructors → Defensive copying` mapping.

```java
import java.util.ArrayList;
import java.util.List;

public class Driver {
    private String name;
    private List<String> licenseEndorsements;

    public Driver(String name, List<String> licenseEndorsements) {
        this.name = name;
        this.licenseEndorsements = licenseEndorsements;
    }

    public List<String> getLicenseEndorsements() {
        return licenseEndorsements;
    }

    public void addEndorsement(String endorsement) {
        licenseEndorsements.add(endorsement);
    }
}
```

**Your task:**

1. Write a short `main()` method that *proves* the bugs exist - construct a `Driver` from a `List<String>` you still hold a reference to, mutate that original list afterward, and show it silently changed the `Driver`'s internal state too. Then show that `getLicenseEndorsements()` itself hands out a list a caller can mutate directly.
2. Fix the issues using only concepts from today - no exceptions, no validation logic yet (that's Day 3).
3. Re-run your proof code from step 1 and confirm it no longer succeeds.

**Hint:** you already have the pattern for this fix in `SecureCodingPrinciplesDemo.java`. You're applying the same fix to a new class, not inventing a new one.

---

## Part 4 - Reflection 

Answer in a few sentences each. No code required.

1. In today's `Vehicle` class, `fuelLevel` and `mileage` are private. If this simulator became a real fleet-management system used by multiple companies, what's one piece of data on a `Vehicle` or `Driver` that absolutely could not be allowed to leak the way `licenseEndorsements` did in Part 3?
2. The `Driver` fix in Part 3 didn't need any new syntax - just applying `private`, constructors, and defensive copying correctly. What does that tell you about where security problems usually come from in real code?
3. Day 10 is a capstone security project. Based only on what you know from today, write one sentence describing a rule you'd want your capstone's domain classes to follow, no matter what the project turns out to be.

---

## Submission checklist

- [ ] `Bus.java` compiles and is wired into `VehicleSimulator`'s menu
- [ ] Fleet `Set`/`Map`/`Queue` features all work from the menu
- [ ] `Driver.java` - proof-of-bug code, plus all fixes applied
- [ ] Part 4 reflection answers (three short written paragraphs)

---
---


```


