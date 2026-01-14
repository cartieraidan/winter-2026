import java.util.ArrayList;
import java.util.Random;

/**
 *Note I'm sure when I rewrite this I can optimize it to only have the recommend agent go and not need to always do all 3. Say
 * T1 miss then T2 hit don't need to do T3 so put to wait, like change its state to not active -> can do that in the thread of the
 * task that hit.
 *
 * @author Aidan Cartier
 * @version Jan 12, 2026
 */
public class Agent extends Thread {

    private final Object lock1 = new Object();
    private final Object lock2 = new Object(); // used for reading
    private volatile ArrayList<Components>  assemblyTable;
    private ArrayList<Thread> techs;
    private int assembledDrones = 0;
    private int techLatch = 0;

    // initialize everything like all threads
    public Agent() {
        assemblyTable = new ArrayList<>();
        techs = new ArrayList<>();

        this.addTechs();
    }

    public void incrementLatch() {
        synchronized (lock1) {
            techLatch++;
            lock1.notify();
        }
    }

    private void addTechs() {
        Technician t1 = new Technician(Components.Frame, this, "T1 (Frame)");
        Technician t2 = new Technician(Components.ControlFirmware, this, "T2 (Firmware)");
        Technician t3 = new Technician(Components.PropulsionUnit, this, "T3 (Propulsion)");

        techs.add(t1);
        techs.add(t2);
        techs.add(t3);
    }

    public void startProduction() {

        this.start();

        for (Thread tech: techs) {
            tech.start();
        }

        this.updateTable();
    }

    // notify techs when table is updated
    private void notifyTech() {
        synchronized (lock1) {
            for (Thread tech : techs) {
                ((Technician) tech).setScheduled(true);

            }
            this.techLatch = 0;
            lock1.notifyAll();

            while (techLatch < techs.size()) {
                try {
                    lock1.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }


        }
        if (techLatch == 3) {
            System.out.println("Going to next combination");
            this.updateTable();
        }

    }

    // get lock for techs when modifying
    public Object getLock1() {
        return lock1;
    }

    public Object getLock2() {
        return lock2;
    }

    // called by techs when done assembling
    public void incrementCount() {
        synchronized (lock1) {
            assembledDrones++;
            System.out.println("Increment count: " + assembledDrones);
            if (assembledDrones == 20) {
                System.out.println("Reached max");
                this.endThreads();
            }


        }

    }

    // so the techs can see what's on the table
    public ArrayList<Components> getAssemblyTable() {
        synchronized (lock1) { // called in tech thread, because it already has a lock it won't get blocked
            return assemblyTable;
        }
    }

    private void updateTable() {

        synchronized (lock1) {
            if (assembledDrones == 20) return;

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

            // test
            System.out.print("Current combination: ");
            for (Components comp : assemblyTable) {
                System.out.print(comp + ", ");
            }
            System.out.println();
            // end test

            notifyTech(); // notifies techs of table has been updated
        }
    }

    // ends tech threads
    private void endThreads() {
        for (Thread tech : techs) { // end tech task
            tech.interrupt();
        }

        this.interrupt(); // end agent task

        System.exit(0);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {


        }
    }

    public static void main(String[] args) {
        Agent agent = new Agent();
        agent.startProduction();
    }
}
