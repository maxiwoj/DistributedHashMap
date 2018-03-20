package distributedmap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InteractiveMapManagementTool {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    DistributedStringMap map;

    public InteractiveMapManagementTool() throws Exception {
        map = new DistributedStringMap();
    }

    public void runCommandLine() throws IOException {
        while (true) {
            System.out.print("> ");
            String command = br.readLine().toLowerCase();
            switch (command) {
                case "put":
                    this.handlePutOperation();
                    break;
                case "read":
                    this.handleReadOperation();
                    break;
                case "has":
                    this.handleContainsOperation();
                    break;
                case "del":
                    this.handleRemoveOperation();
                    break;
            }
        }
    }

    private void handleRemoveOperation() throws IOException {
        System.out.print("Enter key: ");
        String key = br.readLine();
        System.out.println("Removed value: " + map.remove(key));
    }

    private void handleContainsOperation() throws IOException {
        System.out.print("Enter key: ");
        String key = br.readLine();
        System.out.println(map.containsKey(key));
    }

    private void handleReadOperation() throws IOException {
        System.out.print("Enter key: ");
        String key = br.readLine();
        System.out.println(map.get(key));
    }

    private void handlePutOperation() throws IOException {
        System.out.print("Enter Key: ");
        String key = br.readLine();
        System.out.print("Enter Value: ");
        String value = br.readLine();
        this.map.put(key, value);
    }
}
