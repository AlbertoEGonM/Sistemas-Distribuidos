package clase19.clases;/*
 *  MIT License
 *
 *  Copyright (c) 2019 Michael Pogrebinsky - Distributed Systems & Cloud Computing with Java
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */


import clase19.clases.WebClient;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class Aggregator {
    private WebClient webClient;

    public Aggregator() {
        this.webClient = new WebClient();
    }

    public List<String> sendTasksToWorkers(List<String> workersAddresses, List<String> tasks) {
        // Resultados guardados en el mismo orden que la lista de tareas.
        String[] results = new String[tasks.size()];
        // Índice de la siguiente tarea pendiente, compartido por todos los servidores.
        AtomicInteger nextTask = new AtomicInteger(0);

        // Al inicio cada servidor recibe una tarea (8081 -> tarea 1, 8082 -> tarea 2).
        // Cuando un servidor termina, toma la siguiente tarea pendiente.
        CompletableFuture<Void>[] workerChains = new CompletableFuture[workersAddresses.size()];
        for (int i = 0; i < workersAddresses.size(); i++) {
            workerChains[i] = assignNextTask(workersAddresses.get(i), tasks, nextTask, results);
        }

        // Esperamos a que todos los servidores se queden sin tareas.
        CompletableFuture.allOf(workerChains).join();

        return Arrays.asList(results);
    }

    private CompletableFuture<Void> assignNextTask(String workerAddress, List<String> tasks,
                                                   AtomicInteger nextTask, String[] results) {
        int taskIndex = nextTask.getAndIncrement();
        if (taskIndex >= tasks.size()) {
            return CompletableFuture.completedFuture(null);
        }

        String task = tasks.get(taskIndex);
        int port = URI.create(workerAddress).getPort();
        System.out.printf("Tarea %d (%s) asignada al servidor %d%n", taskIndex + 1, task, port);

        byte[] requestPayload = task.getBytes();
        return webClient.sendTask(workerAddress, requestPayload)
                .thenCompose(result -> {
                    results[taskIndex] = result;
                    System.out.printf("El servidor %d terminó la tarea %d (%s)%n", port, taskIndex + 1, task);
                    return assignNextTask(workerAddress, tasks, nextTask, results);
                });
    }
}
