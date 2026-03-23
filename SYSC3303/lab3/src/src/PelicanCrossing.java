/**
 * ============================================================
 * Pelican Crossing State Machine - State Pattern Solution
 * ============================================================
 *
 * Pelican Crossing Controller
 *
 * STUDENT STARTER CODE
 *
 * You MUST NOT:
 *  - Rename this class
 *  - Rename enums or enum values
 *  - Change method signatures
 *  - Remove required fields
 *
 * You MAY:
 *  - Add private helper methods
 *  - Add private classes (State Pattern)
 *  - Add private variables
 *
 * The TA JUnit harness depends on the exact names below.
 *
 * @author Dr. Rami Sabouni, Aidan Cartier
 * Systems and Computer Engineering,
 * Carleton University
 * @version March 23, 2026
 */
public class PelicanCrossing {

    /* =========================================================
     * ENUMS — DO NOT MODIFY NAMES OR VALUES
     * ========================================================= */

    /** Events injected into the state machine */
    public enum Event {
        INIT,
        PEDS_WAITING,
        Q_TIMEOUT,
        OFF,
        ON
    }

    /** Leaf states used for grading and testing */
    public enum State {
        OPERATIONAL_CARS_GREEN_NO_PED,
        OPERATIONAL_CARS_GREEN_PED_WAIT,
        OPERATIONAL_CARS_GREEN_INT,
        OPERATIONAL_CARS_YELLOW,
        OPERATIONAL_PEDS_WALK,
        OPERATIONAL_PEDS_FLASH,

        OFFLINE_FLASH_ON,
        OFFLINE_FLASH_OFF
    }

    /** Output signal for cars */
    public enum CarSignal {
        RED,
        GREEN,
        YELLOW,
        FLASHING_AMBER_ON,
        FLASHING_AMBER_OFF
    }

    /** Output signal for pedestrians */
    public enum PedSignal {
        DONT_WALK_ON,
        DONT_WALK_OFF,
        WALK
    }

    /* =========================================================
     * TIMING CONSTANTS (ticks)
     * DO NOT RENAME — values may be changed if justified
     * ========================================================= */

    public static final int GREEN_TOUT  = 3;   // minimum green duration
    public static final int YELLOW_TOUT = 2;   // yellow duration
    public static final int WALK_TOUT   = 3;   // walk duration
    public static final int PED_FLASH_N = 6;   // number of flashing ticks

    /* =========================================================
     * REQUIRED INTERNAL STATE
     * ========================================================= */

    /** Current leaf state (used by TA tests) */
    private State state;

    /** Output signals (used by TA tests) */
    private CarSignal carSignal;
    private PedSignal pedSignal;

    private boolean isPedestrianWaiting; // boolean if pedestrian walking

    private final VehiclesEnabled vehicleOperations; // reference to class for vehicle operations
    private final PedestrianEnabled pedestrianOperations; // reference to class for pedestrian operations

    private int pedestrianFlashCtr = 0; // counter for the number of flashes for a walk signal

    /* =========================================================
     * CONSTRUCTOR
     * ========================================================= */

    /**
     * Constructor initializes the state machine.
     */
    public PelicanCrossing() {
        // Students should trigger initialization here
        // Example: dispatch(Event.INIT);

        vehicleOperations = new VehiclesEnabled(this);
        pedestrianOperations = new PedestrianEnabled(this);

        this.dispatch(Event.INIT);
    }

    /* =========================================================
     * REQUIRED PUBLIC API — DO NOT CHANGE SIGNATURES
     * ========================================================= */

    /**
     * Inject an event into the state machine.
     */
    public void dispatch(Event e) {
        switch (e) {
            case INIT -> {
                vehicleOperations.vehiclesEnabled();
            }

            case PEDS_WAITING -> {
                this.isPedestrianWaiting = true;

                assert this.state != null;

                if (this.state.equals(State.OPERATIONAL_CARS_GREEN_NO_PED)) { // changes state to pedestrians waiting
                    this.state = State.OPERATIONAL_CARS_GREEN_PED_WAIT;

                } else if (this.state.equals(State.OPERATIONAL_CARS_GREEN_INT)) { // wait request when in green int state
                    vehicleOperations.vehiclesYellow();

                }
            }

            case Q_TIMEOUT -> { // whenever timer exits

                // after green light timeout
                if (this.state == State.OPERATIONAL_CARS_GREEN_PED_WAIT || this.state == State.OPERATIONAL_CARS_GREEN_NO_PED) {

                    if (this.isPedestrianWaiting) { // goes yellow
                        vehicleOperations.vehiclesYellow();

                    } else { // goes green init
                        this.state = State.OPERATIONAL_CARS_GREEN_INT; // waiting for pedestrian wait

                    }

                } else if (this.state == State.OPERATIONAL_CARS_YELLOW) { // for yellow light timeout
                    this.pedestrianOperations.pedestrianEnabled();

                } else if (this.state == State.OPERATIONAL_PEDS_WALK) { // for pedestrian walk timeout
                    this.pedestrianOperations.pedestrianFlash();

                }  else if (this.state == State.OPERATIONAL_PEDS_FLASH) { // for pedestrian flash timeout
                    this.pedestrianFlashCtr--;

                    if (this.pedestrianFlashCtr == 0) { // equal to 0 go back to vehicle operations
                        this.vehicleOperations.vehiclesEnabled();

                    } else if ((this.pedestrianFlashCtr & 1) == 0) { // toggling flash
                        this.pedSignal = PedSignal.DONT_WALK_ON;

                        this.pedestrianOperations.pedestrianFlash();

                    } else { // toggling flash
                        this.pedSignal = PedSignal.DONT_WALK_OFF;

                        this.pedestrianOperations.pedestrianFlash();
                    }

                } else if (this.state == State.OFFLINE_FLASH_ON) { // toggle when in state off/on
                    this.toggleFlashOff();

                } else if (this.state == State.OFFLINE_FLASH_OFF) { // toggle when in state off/on
                    this.toggleFlashOn();
                }
            }

            case OFF -> {
                this.toggleFlashOff();
            }

            case ON -> {
                this.toggleFlashOn();
            }
        }
    }

    /**
     * Convenience method: advance the clock by n ticks.
     * Each tick corresponds to one Q_TIMEOUT event.
     */
    public void tick(int n) {
        int i = 0;
        while (i < n) {
            i++;
        }

        dispatch(Event.Q_TIMEOUT);
    }

    /**
     * Return the current leaf state.
     * Used directly by the TA JUnit harness.
     */
    public State getState() {
        return state;
    }

    /**
     * Return the current car signal.
     */
    public CarSignal getCarSignal() {
        return carSignal;
    }

    /**
     * Return the current pedestrian signal.
     */
    public PedSignal getPedSignal() {
        return pedSignal;
    }

    /**
     * Toggles state and signal to offline on.
     */
    private void toggleFlashOn() {
        this.state = State.OFFLINE_FLASH_ON;

        this.carSignal = CarSignal.FLASHING_AMBER_ON;
        this.pedSignal = PedSignal.DONT_WALK_ON;
    }

    /**
     * Toggles state and signal to offline off.
     */
    private void toggleFlashOff() {
        this.state = State.OFFLINE_FLASH_OFF;

        this.carSignal = CarSignal.FLASHING_AMBER_OFF;
        this.pedSignal = PedSignal.DONT_WALK_OFF;
    }

    /**
     * VehiclesEnabled class is to handle all operations for the vehicle state machine.
     */
    private class VehiclesEnabled {

        private final PelicanCrossing crossing; // reference to crossing class

        /**
         * Constructor initializes class and attribute.
         *
         * @param crossing Reference for PelicanCrossing.
         */
        public VehiclesEnabled(PelicanCrossing crossing) {
            this.crossing = crossing;
        }

        /**
         * State inside vehicle state machine when first initialized.
         */
        void vehiclesEnabled() {
            crossing.pedSignal = PedSignal.DONT_WALK_ON;
            this.vehiclesGreen();
        }

        /**
         * Action to turn light green and update state.
         */
        void vehiclesGreen() {
            crossing.carSignal = CarSignal.GREEN; // set light to green
            crossing.state = State.OPERATIONAL_CARS_GREEN_NO_PED; // update current state

            crossing.isPedestrianWaiting = false;

            //crossing.tick(GREEN_TOUT); // set timeout to 3 ticks
        }

        /**
         * State inside vehicle when goes to yellow light.
         */
        void vehiclesYellow() {
            crossing.state = State.OPERATIONAL_CARS_YELLOW;
            crossing.isPedestrianWaiting = false; // set back to false since don't need it anymore

            crossing.carSignal = CarSignal.YELLOW;

            //crossing.tick(YELLOW_TOUT); // set timeout to 2
        }
    }

    /**
     * PedestrianEnabled class is to handle all operations for the pedestrian state machine.
     */
    private class PedestrianEnabled {

        private final PelicanCrossing crossing; // reference to crossing class

        /**
         * Constructor initializes class and attribute.
         *
         * @param crossing Reference for PelicanCrossing.
         */
        public PedestrianEnabled(PelicanCrossing crossing) {
            this.crossing = crossing;
        }

        /**
         * State inside pedestrian when first enters.
         */
        void pedestrianEnabled() {
            crossing.carSignal = CarSignal.RED;

            this.pedestrianWalk();
        }

        /**
         * Action of enabling walking and updating state.
         */
        void pedestrianWalk() {
            crossing.state = State.OPERATIONAL_PEDS_WALK;
            crossing.pedSignal = PedSignal.WALK;

            //crossing.tick(WALK_TOUT);
        }

        /**
         * State for when pedestrian light is flashing.
         */
        void pedestrianFlash() {
            if (crossing.state != State.OPERATIONAL_PEDS_FLASH) {
                crossing.state = State.OPERATIONAL_PEDS_FLASH;
                crossing.pedestrianFlashCtr = 7;
                crossing.pedSignal = PedSignal.DONT_WALK_ON;
            }

            //crossing.tick(PED_FLASH_N);
        }

    }

    /* =========================================================
     * STUDENT NOTES
     * =========================================================
     *
     * - You may implement this using:
     *     • enum + switch
     *     • State Pattern (recommended)
     * - You may add:
     *     • private helper methods
     *     • private counters/timers
     *     • private inner classes
     *
     * - You MUST ensure:
     *     • OFF works from any operational state
     *     • ON works from any offline state
     *     • getState(), getCarSignal(), getPedSignal()
     *       always reflect the CURRENT behavior
     *
     * - The TA JUnit harness will:
     *     • inject events in arbitrary order
     *     • call tick(n)
     *     • assert exact states and outputs
     */
}