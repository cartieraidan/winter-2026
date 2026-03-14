import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The class EventLogger uses a buffer to log and flush logs into text files by threads and then prints
 * out a statement of statistics.
 *
 * @author Aidan Cartier
 * @version March 14, 2026
 */
public class EventLogger {

    private final long startTime;

    private final Queue<String> logBuffer;
    private final ScheduledExecutorService executor;

    /**
     * Constructor for initializing buffer, and single thread executor to flush buffer periodically
     * and at shutdown.
     */
    public EventLogger() {
        startTime = System.nanoTime();
        this.logBuffer = new ConcurrentLinkedQueue<>();

        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true); // not prevent jvm
            return t; // returning thread to executor
        });

        try { // overwrites if existing file
            FileWriter fw = new FileWriter("eventsLog.txt"); // clears file
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // creating periodic task
        executor.scheduleAtFixedRate(this::flushLogs, 0, 10, TimeUnit.MILLISECONDS);

        // at shutdown flush all logs
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executor.shutdown();
            flushLogs();
            analytics();
            })
        );
    }

    /**
     * Method log is used to add log to buffer.
     *
     * @param identifier String name of thread.
     * @param event String event that thread logged.
     * @param data String of any relevant data associated to log.
     */
    public void log(String identifier, String event, String data) {
        double time = (System.nanoTime() - startTime) / 1_000_000.0;

        String log;

        if (data != null) {
            log = "Event log: [" + String.format("%.3f", time) + " ms, " + identifier + ", " + event + ", " + data + "]";
        } else {
            log = "Event log: [" + String.format("%.3f", time) + " ms, " + identifier + ", " + event + "]";
        }

        logBuffer.add(log);
    }

    /**
     * Method overload as some logs do no require the data param, therefore, it passes it as null.
     *
     * @param identifier String name of thread.
     * @param event String event that thread logged.
     */
    public void log(String identifier, String event) {
        this.log(identifier, event, null);
    }

    /**
     * Method that is executed at shutdown the flush any remaining logs
     */
    private synchronized void flushLogs() {
        try (FileWriter fw = new FileWriter("eventsLog.txt", true)) { // appending to file
            String log;
            while ((log = logBuffer.poll()) != null) {
                fw.write(log + "\n");

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method call at shutdown to print statistics.
     */
    private void analytics() {
        Path path = Paths.get("eventsLog.txt");

        try {
            BufferedReader file = Files.newBufferedReader(path);
            this.bigBallOfStats(file);
        } catch (IOException e) {
            System.err.println("Error reading eventsLog.txt");
        }
    }

    /**
     * Method required a lot of refactoring and duplication therefore just kept it as one big mess.
     *
     * @param file BufferedReader of file calculating stats.
     * @throws IOException Throws if can't read file.
     */
    private void bigBallOfStats(BufferedReader file) throws IOException {

        int count = 0;
        StringBuilder threads = new StringBuilder();
        Map<String, RecordTime> responseTime = Map.of();

        double startTime = 0.0;

        int dronesAssembled = 0;
        double completeTime = 0.0;

        String line;
        while ((line = file.readLine()) != null) { // going through each line

            count++;

            // getting rid of useless data
            line = line.replace("Event log: ", "");
            line = line.replace("[", "");
            line = line.replace("]", "");

            String[] data = line.split(",");

            if (count < 5) { // appending threads names
                threads.append(data[1]).append("#");

            } else if (count == 5) { // adding to a map
                responseTime = this.initMap(threads.toString());

            } else if (count > 6) { // starts counting after first notifyAll

                String event = data[2].trim();
                double time = Double.parseDouble(data[0].replace(" ms", ""));

                if (event.equals("NotifyAll")) {
                    startTime = time;
                }
                if (event.equals("Waiting")
                        || event.equals("Picked Components")
                        || event.equals("Placed Components"))
                { // for response time
                    // from notifyAll waiting = blocked, picked component = assembled drone, place components = put in table

                    RecordTime newRecord = responseTime.get(data[1]);
                    newRecord.addTime(time - startTime); // updates object its mapped to already

                    //responseTime.replace(data[1],
                        //    responseTime.get(data[1]),
                         //   newRecord);
                }

                // for tech & agent
                if (event.equals("Waiting")) { // for utilization, calc done in object
                    responseTime.get(data[1]).addWaitTime(time); // adding time when started waiting

                } else if (event.equals("Picked Components")) {
                    responseTime.get(data[1]).addBusyTime(time); // adding time when busy started

                } else if (event.equals("Placed Components")) { // for agent busy cycle
                    responseTime.get(data[1]).addBusyTime(time);

                }

                if (event.equals("Drone Assembled")) { // for throughput
                    dronesAssembled++;

                    if (dronesAssembled == 20) {
                        completeTime = time;
                    }
                }
            }


            for (String s : data) {
                System.out.print(s + ", ");
            }
            System.out.println();



        }

        // where print stats
        System.out.println("###################################");

        for (Map.Entry<String, RecordTime> entry : responseTime.entrySet()) {
            System.out.println("Response time of " + entry.getKey() + ": " + String.format("%.3f",entry.getValue().totalTime / entry.getValue().count) + " ms");
            System.out.println("Total busy time of " + entry.getKey() + ": " + String.format("%.3f",entry.getValue().totalBusyTime) + " ms");
            System.out.println("Total wait time of " + entry.getKey() + ": " + String.format("%.3f",entry.getValue().totalWaitTime) + " ms");
            System.out.println();
        }

        //System.out.println(responseTime);
        System.out.println("Throughput of assembling 20 drones: 1 drone every " +  String.format("%.3f",completeTime / 20) + " ms");
        System.out.println("System finished at: " + completeTime + " ms");
    }

    /**
     * Method used to initialize map with given thread names in a string.
     *
     * @param threads String of thread names separated by #.
     * @return Returns map with thread names mapped to RecordTime object.
     */
    private Map<String, RecordTime> initMap(String threads) {
        String[] data = threads.split("#");

        Map<String, RecordTime> responseTime = new HashMap<>();

        for (String name : data) {
            responseTime.put(name, new RecordTime());
        }

        return responseTime;
    }

    /**
     * Class used for recording and calculating response time and utilization of each thread.
     */
    private static class RecordTime {
        double totalTime;
        int count;

        double waitTimeStart, busyTimeStart;
        double totalWaitTime, totalBusyTime;

        boolean init =  false;

        /**
         * Constructor sets everything to 0.
         */
        public RecordTime() {
            this.totalTime = 0.0;
            this.count = 0;

            this.waitTimeStart = 0.0;
            this.busyTimeStart = 0.0;
            this.totalWaitTime = 0.0;
            this.totalBusyTime = 0.0;
        }

        /**
         * Method adds new wait time and calculates total busy time spent.
         * @param waitTime Double of time.
         */
        public void addWaitTime(double waitTime) {
            this.waitTimeStart = waitTime;

            if (!init) { // if other var time hasn't been init
                init = true;

            } else {
                this.totalBusyTime += this.waitTimeStart - this.busyTimeStart;
            }
        }

        /**
         * Method adds new busy time and calculates total wait time spent.
         * @param busyTime Double of time.
         */
        public void addBusyTime(double busyTime) {
            this.busyTimeStart = busyTime;

            if (!init) { // if other var time hasn't been init
                init = true;

            }  else {
                this.totalWaitTime += this.busyTimeStart - this.waitTimeStart;
            }
        }

        /**
         * Method adds response time and increments event counting for averaging.
         * @param time Double of time.
         */
        public void addTime(Double time) {
            this.totalTime += time;
            this.count++;
        }

        /**
         * Method for string representation.
         * @return String representation of object
         */
        public String toString() {
            return "(" + String.format("%.3f", this.totalTime) + ", " + this.count + ", total wait time: " + String.format("%.3f", this.totalWaitTime) + ", total busy time: " + String.format("%.3f", this.totalBusyTime) + ")";
        }
    }

}
