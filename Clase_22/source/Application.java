import java.util.*;

public class Application {
    public static void main(String[] args) throws Exception {
        String hash = args.length > 0 ? args[0] : FuerzaBrutaHash.sha256("abaco");
        List<String> workers = args.length > 1 ? Arrays.asList(Arrays.copyOfRange(args, 1, args.length))
                : List.of("http://localhost:8081/task", "http://localhost:8082/task");
        List<String> tasks = Aggregator.createTasks(hash);
        long start = System.nanoTime();
        List<Aggregator.Result> results = new Aggregator().sendTasksToWorkers(workers, tasks);
        System.out.print(Aggregator.report(results, (System.nanoTime() - start) / 1e9));
    }
}
