import java.util.ArrayList;
import java.util.Random;

/**
 *
 *
 * @author Aidan Cartier
 * @version Jan 12, 2026
 */
public class Agent extends Thread {

    private final Object lock = new Object(); // may add second lock for concurrency depending on task
    private volatile ArrayList<Components>  assemblyTable;
    private ArrayList<Thread> techs;
    private int assembledDrones = 0;

    // initialize everything like all threads
    public Agent() {
        assemblyTable = new ArrayList<>();
        techs = new ArrayList<>();


    }

    // notify techs when table is updated
    private void notifyTech() {
        synchronized (lock) {
            for (Thread tech : techs) {
                tech.notify();
                ((ObservableTech) tech).tableUpdated();
            }
        }
    }

    // get lock for techs when modifying
    public Object getLock() {
        return lock;
    }

    // called by techs when done assembling
    public void incrementCount() {
        synchronized (lock) {
            if (assembledDrones++ >= 20) {
                this.endThreads();
            } else {
                this.updateTable();
            }
        }

    }

    // so the techs can see what's on the table
    public ArrayList<Components> getAssemblyTable() {
        return assemblyTable;
    }

    private void updateTable() {

        synchronized (lock) {
            assemblyTable.clear(); // clears previous old components
            Random rand = new Random();

            while (assemblyTable.size() < 2) { // adds two unique random components to the assembly table
                if (!assemblyTable.isEmpty()) {
                    Components comp = Components.values()[rand.nextInt(Components.values().length)];
                    if (comp != assemblyTable.getFirst()) {
                        assemblyTable.add(comp);
                    }

                } else {
                    assemblyTable.add(Components.values()[rand.nextInt(Components.values().length)]);
                }
            }

            notifyTech(); // notifies techs of table has been updated
        }
    }

    // ends tech threads
    private void endThreads() {
        for (Thread tech : techs) { // end tech task
            tech.interrupt();
        }

        this.interrupt(); // end agent task
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                this.wait(); // don't know if this is good
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
