package home;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class ProducerRunner {
    private ProducerRunner() { }
    public static void run(int threads, List<Runnable> producers) throws InterruptedException, ExecutionException {
        if (threads < 1) throw new IllegalArgumentException("Thread count must be positive");
        var executor = Executors.newFixedThreadPool(threads);
        try {
            var tasks = producers.stream().<Callable<Void>>map(p -> () -> { p.run(); return null; }).toList();
            var futures = executor.invokeAll(tasks);
            for (var future : futures) future.get();
        } finally {
            executor.shutdown();
            try {
                while (!executor.awaitTermination(1, TimeUnit.SECONDS)) { /* Drain accepted work. */ }
            } catch (InterruptedException ex) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
                throw ex;
            }
        }
    }
}
