import java.util.ArrayList;

/**
 * Class where all the shared information is kept. Producers put into the table and consumers get out
 * of the table.
 *
 * @author Aidan Cartier
 * @version Jan 14, 2026
 */
public class AssemblyTable {

    private ArrayList<Components> table;

    private final Object readLock;
    //private final Object readLock;

    private final EventLogger logger;

    /**
     * Constructor for shared section.
     */
    public AssemblyTable(EventLogger logger) {
        table = new ArrayList<>();
        this.logger = logger;

        readLock = new Object();
       // readLock = new Object();
    }

    /**
     * Classes require lock to read or modify state.
     *
     * @return Return object acting as key.
     */
    public Object getReadLock() {
        return readLock;
    }

    //public Object getReadLock() {
    //    return readLock;
   // }

    /**
     * Method used by Agent to add new components when table is empty.
     *
     * @param components ArrayList of components, expected length is 2.
     * @throws InterruptedException Calls object.wait() therefore needs to be caught somewhere else.
     */
    public void put(ArrayList<Components> components, String name) throws InterruptedException {
        synchronized (readLock) {
            while (!table.isEmpty()) { // table not empty = wait
                this.logger.log(name, "Waiting");
                readLock.wait();
            }
            if (Thread.currentThread().isInterrupted()) return;
            table = components; // new components
            this.logger.log(name, "Placed Components", "(" + components.getFirst().toString() + ", " + components.getLast().toString() + ")");

            System.out.println("table updated");
            this.logger.log(name, "NotifyAll");
            readLock.notifyAll(); // wake up everyone waiting
        }
    }

    /**
     * Method used by technicians to get the components in the table.
     *
     * @return ArrayLIst of components on current table.
     * @throws InterruptedException Calls object.wait() therefore needs to be caught somewhere else.
     */
    public ArrayList<Components> get(String name) throws InterruptedException {
        synchronized (readLock) {
            while (table.isEmpty()) { // table empty = wait
                if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
                this.logger.log(name, "Waiting");
                readLock.wait();
            }

            return table;
        }

    }

    /**
     * Method removes current components on table.
     */
    public void signalAssembled(String name) {
        synchronized (readLock) {
            System.out.println("Table deleted");
            table.clear();

            this.logger.log(name, "NotifyAll");
            readLock.notifyAll();
        }

    }
}
