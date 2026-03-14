import java.io.FileWriter;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EventLogger {

    private final long startTime;

    private final Queue<String> logBuffer;
    private final ScheduledExecutorService executor;

    public EventLogger() {
        startTime = System.nanoTime();
        this.logBuffer = new ConcurrentLinkedQueue<>();

        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true); // not prevent jvm
            return t; // returning thread to executor
        });

        try {
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
            })
        );
    }

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

    public void log(String identifier, String event) {
        this.log(identifier, event, null);
    }

    private synchronized void flushLogs() {
        try (FileWriter fw = new FileWriter("eventsLog.txt", true)) {
            String log;
            while ((log = logBuffer.poll()) != null) {
                fw.write(log + "\n");

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
