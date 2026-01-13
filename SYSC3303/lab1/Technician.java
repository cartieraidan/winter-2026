import java.util.ArrayList;

/**
 *
 *
 * @author Aidan Cartier
 * @version Jan 12, 2026
 */
public class Technician extends Thread  {

    private boolean scheduled = false;
    private Components inheritedComponent;
    private final Agent agent;
    private final Object lock1;
    private final Object lock2;
    private String name;

    public Technician(Components inheritedComponent, Agent agent,  String name) {
        this.name = name;
        this.inheritedComponent = inheritedComponent;
        this.agent = agent;
        this.lock1 = agent.getLock1();
        this.lock2 = agent.getLock2();
    }

    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (lock1) {
                while (!scheduled) {
                    try {
                        lock1.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);

                    }
                }
                // implementation
                System.out.println(name + " Scheduled");
                if (this.checkValidCombination()) {
                    System.out.println(name + " is valid");
                    this.assembleDrone();
                }
                // when done logic
                agent.incrementLatch();
                this.scheduled = false;
            }
        }

    }

    private void assembleDrone() {
        synchronized (lock1) {
            agent.incrementCount();
        }
    }

    private boolean checkValidCombination() {
        synchronized (lock1) {
            ArrayList<Components> assemblyTable = agent.getAssemblyTable();
            for (Components component : assemblyTable) {
                if (component.equals(inheritedComponent)) {
                    return false;
                }
            }
            return true;
        }

    }
}
