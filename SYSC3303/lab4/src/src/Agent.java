import java.util.ArrayList;
import java.util.Random;

/**
 * Class Agent updates the assembly table as a producer with a random combination of
 * components.
 *
 * @author Aidan Cartier
 * @version Jan 14, 2026
 */
public class Agent extends Thread {

    private int assembledDrones = 0;

    private final AssemblyTable assemblyTable;
    private final ProductionController controller;

    private final Object readLock;

    /**
     * Constructor to create Agent class, updates AssemblyTable when empty.
     *
     * @param assemblyTable Reference to assembly table Agent will put to.
     * @param controller Reference to controller for Agent to notify when assembled all drones.
     */
    public Agent(AssemblyTable assemblyTable, ProductionController controller) {
        this.assemblyTable = assemblyTable;
        this.controller = controller;

        this.readLock = assemblyTable.getReadLock(); // get lock from shared memory
    }

    /**
     * Method increments count of drones assembled and signals controller to end all threads
     * if reached max amount.
     */
    public void incrementCount() {
        //synchronized (readLock) { // dont think need sync
            assembledDrones++;
            System.out.println("Increment count: " + assembledDrones);
            if (assembledDrones == 20) { // if reached max
                System.out.println("Reached max");
                this.controller.endThreads(); // interrupts all threads
            }

        //}

    }

    /**
     * Method updateTable() chooses two unique random components and adds them to the AssemblyTable.
     */
    private void updateTable() {

        //synchronized (readLock) {
            if (assembledDrones == 20) controller.endThreads(); // catch case if something got missed

            ArrayList<Components> components = new ArrayList<>();
            Random rand = new Random();

            while (components.size() < 2) { // adds two unique random components to the assembly table
                if (!components.isEmpty()) { // if not empty (already added first comp.)
                    Components comp = Components.values()[rand.nextInt(Components.values().length)];
                    if (comp != components.getFirst()) { // checks they're unique
                        components.add(comp);
                    }

                } else {
                    components.add(Components.values()[rand.nextInt(Components.values().length)]);
                }
            }

            try { // try and add to table
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


        //}
    }

    /**
     * Method override from Thread extension.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) { // continue running until thread interrupt flag
            synchronized (readLock) {
                System.out.println("updating table potentially");
                this.updateTable();
            }

        }
    }
}
