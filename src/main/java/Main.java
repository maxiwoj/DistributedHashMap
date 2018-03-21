import distributedmap.InteractiveMapManagementTool;

import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Created by lab on 3/19/2018.
 */
public class Main {
    public static final Level LOGGING_LEVEL = Level.WARNING;

    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().getLogger("").setLevel(LOGGING_LEVEL);
        InteractiveMapManagementTool interactiveMapManagementTool = new InteractiveMapManagementTool();
        interactiveMapManagementTool.runCommandLine();
    }
}
