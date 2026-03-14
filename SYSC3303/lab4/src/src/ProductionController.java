import java.util.ArrayList;

/**
 * Class where it controls the creation and management of threads.
 *
 * @author Aidan Cartier
 * @version Jan 14, 2026
 */
public class ProductionController {

    private ArrayList<Thread> techs;
    private Agent agent;
    private boolean destroy = false;
    private EventLogger logger;

    /**
     * Constructor that setups the environment for assignment 1.
     */
    public ProductionController() {
        this.techs = new ArrayList<>();

        this.logger = new EventLogger();

        AssemblyTable assemblyTable = new AssemblyTable(this.logger);

        agent = new Agent(assemblyTable, this,  this.logger);
        Technician t1 = new Technician(Components.Frame, assemblyTable, agent, "t1", this.logger);
        Technician t2 = new Technician(Components.ControlFirmware, assemblyTable, agent, "t2",  this.logger);
        Technician t3 = new Technician(Components.PropulsionUnit, assemblyTable, agent, "t3",   this.logger);
        techs.add(t1);
        techs.add(t2);
        techs.add(t3);

    }

    /**
     * Method starts all threads.
     */
    public void startProduction() {

        agent.start();

        for (Thread tech : techs) {
            tech.start();
        }
    }

    /**
     * method ends all threads when done.
     */
    public void endThreads() {
        if (!destroy) {
            destroy = true;
            for (Thread tech : techs) { // end tech task
                tech.interrupt();
            }

            agent.interrupt(); // end agent task

            //System.exit(0);
        }
    }

    public static void main() {
        ProductionController controller = new ProductionController();

        try {
            controller.startProduction();
        } catch (RuntimeException e) {
            //call the deconstructor here worse case
            controller.endThreads();
        }

    }

}
