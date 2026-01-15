import java.util.ArrayList;

public class ProductionController {

    private ArrayList<Thread> techs;
    private Agent agent;
    private boolean destroy = false;

    public ProductionController() {
        this.techs = new ArrayList<>();

        AssemblyTable assemblyTable = new AssemblyTable();

        agent = new Agent(assemblyTable, this);
        Technician t1 = new Technician(Components.Frame, assemblyTable, agent, "t1");
        Technician t2 = new Technician(Components.ControlFirmware, assemblyTable, agent, "t2");
        Technician t3 = new Technician(Components.PropulsionUnit, assemblyTable, agent, "t3");
        techs.add(t1);
        techs.add(t2);
        techs.add(t3);

    }

    public void startProduction() {

        agent.start();

        for (Thread tech : techs) {
            tech.start();
        }
    }

    public void endThreads() {
        if (!destroy) {
            destroy = true;
            for (Thread tech : techs) { // end tech task
                tech.interrupt();
            }

            agent.interrupt(); // end agent task

            System.exit(0);
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
