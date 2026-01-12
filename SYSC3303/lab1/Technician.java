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
    private final Object lock;

    public Technician(Components inheritedComponent, Agent agent) {
        this.inheritedComponent = inheritedComponent;
        this.agent = agent;
        this.lock = agent.getLock();
    }

    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (lock) {
                while (!scheduled) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                // implementation
                if (this.checkValidCombination()) {
                    this.assembleDrone();
                }
                // when done logic
                this.scheduled = false;
            }
        }

    }

    private void assembleDrone() {
        agent.incrementCount();
    }

    private boolean checkValidCombination() {
        synchronized (lock) {
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
