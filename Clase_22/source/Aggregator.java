import networking.WebClient;
import java.util.*;
import java.util.concurrent.*;

/** Una cadena asincrona por worker; el estado pertenece a una sola consulta. */
public class Aggregator {
    private final WebClient client = new WebClient();

    public static List<String> createTasks(String hash) {
        if (hash == null || !hash.matches("[0-9a-fA-F]{64}"))
            throw new IllegalArgumentException("El hash debe tener 64 caracteres hexadecimales");
        List<String> tasks = new ArrayList<>();
        for (char letter = 'a'; letter <= 'z'; letter++) tasks.add(hash + "," + letter);
        return tasks;
    }

    public static class Result {
        public final String task, worker, value;
        public final int index;
        Result(int index, String task, String worker, String value) {
            this.index = index; this.task = task; this.worker = worker; this.value = value;
        }
    }

    public List<Result> sendTasksToWorkers(List<String> workers, List<String> tasks) {
        if (workers.isEmpty()) throw new IllegalArgumentException("Se requiere al menos un worker");
        State state = new State(tasks);
        CompletableFuture<?>[] chains = new CompletableFuture<?>[workers.size()];
        for (int i = 0; i < workers.size(); i++) chains[i] = next(workers.get(i), state);
        // Se dejan terminar las tareas ya enviadas para devolver sus resultados tambien.
        CompletableFuture.allOf(chains).join();
        state.results.sort(Comparator.comparingInt(r -> r.index));
        return state.results;
    }

    private static class State {
        final List<String> tasks;
        final List<Result> results = new ArrayList<>();
        int index;
        boolean stopped;
        State(List<String> tasks) { this.tasks = List.copyOf(tasks); }
    }

    private CompletableFuture<Void> next(String worker, State state) {
        synchronized (state) {
            if (state.stopped || state.index >= state.tasks.size())
                return CompletableFuture.completedFuture(null);
            int index = state.index++;
            String task = state.tasks.get(index);
            System.out.printf("Tarea %d (%s) -> %s%n", index + 1, task, worker);
            return client.sendTask(worker, task).handle((value, error) -> {
                synchronized (state) {
                    if (error != null) {
                        state.stopped = true;
                        throw new CompletionException(error);
                    }
                    if (!value.equals("NULL")) {
                        String hash = task.substring(0, 64);
                        try {
                            if (!value.matches("[a-z]{5}") || value.charAt(0) != task.charAt(65)
                                    || !FuerzaBrutaHash.sha256(value).equalsIgnoreCase(hash))
                                throw new IllegalStateException("Respuesta invalida del worker " + worker);
                        } catch (Exception ex) {
                            state.stopped = true;
                            throw new CompletionException(ex);
                        }
                        state.stopped = true;
                    }
                    state.results.add(new Result(index, task, worker, value));
                    System.out.printf("Tarea %d terminada: %s%n", index + 1, value);
                }
                return value;
            }).thenCompose(ignored -> next(worker, state));
        }
    }

    public static String report(List<Result> results, double seconds) {
        StringBuilder out = new StringBuilder();
        String password = "NULL";
        for (Result r : results) {
            out.append(String.format("Tarea %d (%s) [%s]: %s%n", r.index + 1, r.task, r.worker, r.value));
            if (!r.value.equals("NULL")) password = r.value;
        }
        out.append("Contrasena: ").append(password).append('\n');
        out.append("Tareas realizadas: ").append(results.size()).append(" / 26\n");
        out.append(String.format(Locale.ROOT, "Tiempo total: %.6f segundos%n", seconds));
        return out.toString();
    }
}
