package Demo;

import Client.Client;
import Server.Server;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO: Add complexity and do a refactoring.
public class Demo {
    public static void main(String[] args) {
        Server server = new Server(0, "Server #" + 0);
        server.addWorkers(10, 100);
        Client client = new Client(0, "Client #" + 0, 10, server);
        ExecutorService executorService = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
        executorService.execute(server);
        executorService.execute(client);
    }
}
