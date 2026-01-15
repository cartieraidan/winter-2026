import java.util.ArrayList;

/**
 *
 * @author Aidan Cartier
 * @version Jan 14, 2026
 */
public class AssemblyTable {

    private ArrayList<Components> table;

    private final Object readLock;
    //private final Object readLock;

    public AssemblyTable() {
        table = new ArrayList<>();

        readLock = new Object();
       /// readLock = new Object();
    }

    public Object getReadLock() {
        return readLock;
    }

    //public Object getReadLock() {
    //    return readLock;
   // }

    public void put(ArrayList<Components> components) throws InterruptedException {
        synchronized (readLock) {
            while (!table.isEmpty()) {
                readLock.wait();
            }
            table = components;
            System.out.println("table updated");
            readLock.notifyAll();
        }
    }

    public ArrayList<Components> get() throws InterruptedException {
        synchronized (readLock) {
            while (table.isEmpty()) {
                readLock.wait();
            }

            return table;
        }

    }

    public void signalAssembled() {
        synchronized (readLock) {
            System.out.println("Table deleted");
            table.clear();

            readLock.notifyAll();
        }

    }
}
