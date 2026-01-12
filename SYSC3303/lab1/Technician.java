/**
 *
 *
 * @author Aidan Cartier
 * @version Jan 12, 2026
 */
public class Technician extends Thread implements ObservableTech {

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {

        }

    }

    @Override
    public void tableUpdated() {
        if (checkValidCombination()) {

        } else {
            // this.wait(); must be lock.wait()
        }
    }

    private boolean checkValidCombination() {
        return true;
    }
}
