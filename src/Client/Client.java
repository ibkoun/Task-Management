package Client;

import Server.Server;
import java.util.Random;

// Responsible for submitting tasks.
public class Client implements Runnable {
    private int id, tasksCount;
    private String name;
    private Server server;
    private final Random random = new Random();

    public Client(int id, String name, int tasksCount, Server server) {
        this.id = id;
        this.name = name;
        this.tasksCount = tasksCount;
        this.server = server;
    }

    @Override
    public void run() {
        int count = 0;
        for (int i = 0; i < tasksCount; ++i) {
            double work = random.nextDouble() * 1000 + 1000;
            int requiredWorkersCount = random.nextInt(6) + 1;
            server.addTask(work, requiredWorkersCount);
            ++count;
        }
        if (count < tasksCount) {
            System.out.println("Missing task");
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
