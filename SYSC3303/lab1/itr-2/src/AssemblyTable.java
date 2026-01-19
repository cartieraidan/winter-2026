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

    /**
     * Constructor for shared section.
     */
    public AssemblyTable() {
        table = new ArrayList<>();

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
    public void put(ArrayList<Components> components) throws InterruptedException {
        synchronized (readLock) {
            while (!table.isEmpty()) { // table not empty = wait
                readLock.wait();
            }
            table = components; // new components
            System.out.println("table updated");
            readLock.notifyAll(); // wake up everyone waiting
        }
    }

    /**
     * Method used by technicians to get the components in the table.
     *
     * @return ArrayLIst of components on current table.
     * @throws InterruptedException Calls object.wait() therefore needs to be caught somewhere else.
     */
    public ArrayList<Components> get() throws InterruptedException {
        synchronized (readLock) {
            while (table.isEmpty()) { // table empty = wait
                readLock.wait();
            }

            return table;
        }

    }

    /**
     * Method removes current components on table.
     */
    public void signalAssembled() {
        synchronized (readLock) {
            System.out.println("Table deleted");
            table.clear();

            readLock.notifyAll();
        }

    }
}
