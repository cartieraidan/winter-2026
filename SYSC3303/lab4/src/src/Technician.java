import java.util.ArrayList;

/**
 * Class that describes the behavior of a technician on the assembly line where it looks for
 * the correct combination to assemble a drone.
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

    /**
     * Constructor for Technician which consumes from AssemblyTable and signals Agent when assembled.
     * (probably could have had a flag that signals in technician that it has assembled and requires agent to
     * reset it)
     *
     * @param inheritedComponent Component that the technician will always have and use to create a match.
     * @param assemblyTable Reference to AssemblyTable to consume components.
     * @param agent Reference to Agent to signal when drone assembled.
     * @param name Name just for debugging purposes.
     */
    public Technician(Components inheritedComponent, AssemblyTable assemblyTable, Agent agent, String name) {
        this.inheritedComponent = inheritedComponent;
        this.assemblyTable = assemblyTable;
        this.agent = agent;
        this.name = name;

        this.readLock = assemblyTable.getReadLock(); // get lock from shared section
        //this.readLock = assemblyTable.getReadLock();
    }

    /**
     * Method that checks valid combination on the table.
     *
     * @return True if the all components unique, false otherwise.
     */
    private boolean checkValidCombination() {
        //synchronized (readLock) {

            try {
                ArrayList<Components> table = assemblyTable.get();

                for (Components component : table) { // checks if all components are unique
                    if (component.equals(inheritedComponent)) {
                        return false;
                    }
                }
                return true;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }



        //}

    }

    /**
     * Method that signals assembly table to take off components from table
     * and signal agent to increment count.
     */
    private void assembleDrone() {
        //synchronized (readLock) {
            assemblyTable.signalAssembled();
            agent.incrementCount();
        //}
    }

    /**
     * Method override from Thread extension.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) { // continue running until thread interrupt flag
            synchronized (readLock) {

                // implementation
                System.out.println(name + " Scheduled");
                if (this.checkValidCombination()) { // if valid combo assemble drone
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
