import java.util.ArrayList;
import java.util.Random;

/**
 * A little cleaner setup, now try to delete sync clause and see what happens
 * then add all comments and docs and clean code
 *
 * @author Aidan Cartier
 * @version Jan 14, 2026
 */
public class Agent extends Thread {

    private int assembledDrones = 0;

    private final AssemblyTable assemblyTable;
    private final ProductionController controller;

    private final Object readLock;

    public Agent(AssemblyTable assemblyTable, ProductionController controller) {
        this.assemblyTable = assemblyTable;
        this.controller = controller;

        this.readLock = assemblyTable.getReadLock();
    }

    public void incrementCount() {
        synchronized (readLock) { // dont think need sync
            assembledDrones++;
            System.out.println("Increment count: " + assembledDrones);
            if (assembledDrones == 20) {
                System.out.println("Reached max");
                this.controller.endThreads();
            }

        }

    }

    private void updateTable() {

        synchronized (readLock) {
            if (assembledDrones == 20) controller.endThreads();

            ArrayList<Components> components = new ArrayList<>();
            Random rand = new Random();

            while (components.size() < 2) { // adds two unique random components to the assembly table
                if (!components.isEmpty()) {
                    Components comp = Components.values()[rand.nextInt(Components.values().length)];
                    if (comp != components.getFirst()) {
                        components.add(comp);
                    }

                } else {
                    components.add(Components.values()[rand.nextInt(Components.values().length)]);
                }
            }

            try {
                assemblyTable.put(components);
                //this.incrementCount();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // test
            System.out.print("Current combination: ");
            for (Components comp : components) {
                System.out.print(comp + ", ");
            }
            System.out.println();
            // end test


        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (readLock) {
                System.out.println("updating table potentially");
                this.updateTable();
            }

        }
    }
}
