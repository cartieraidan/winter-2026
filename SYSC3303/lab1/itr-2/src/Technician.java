import java.util.ArrayList;

/**
 *
 * @author Aidan Cartier
 * @version Jan 14, 2026
 */
public class Technician extends Thread {

    private final Components inheritedComponent;
    private final AssemblyTable assemblyTable;
    private final String name;
    private final Agent agent;

    private final Object readLock;
    //private final Object readLock;

    public Technician(Components inheritedComponent, AssemblyTable assemblyTable, Agent agent, String name) {
        this.inheritedComponent = inheritedComponent;
        this.assemblyTable = assemblyTable;
        this.agent = agent;
        this.name = name;

        this.readLock = assemblyTable.getReadLock();
        //this.readLock = assemblyTable.getReadLock();
    }



    private boolean checkValidCombination() {
        synchronized (readLock) {

            try {
                ArrayList<Components> table = assemblyTable.get();

                for (Components component : table) {
                    if (component.equals(inheritedComponent)) {
                        return false;
                    }
                }
                return true;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }



        }

    }

    private void assembleDrone() {
        synchronized (readLock) {
            assemblyTable.signalAssembled();
            agent.incrementCount();
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (readLock) {

                // implementation
                System.out.println(name + " Scheduled");
                if (this.checkValidCombination()) {
                    System.out.println(name + " is valid");
                    this.assembleDrone();
                } else {
                    try {
                        readLock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        }
    }
}
