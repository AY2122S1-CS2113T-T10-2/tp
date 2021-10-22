package terminus;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.nio.file.Path;
import terminus.command.Command;
import terminus.command.CommandResult;
import terminus.common.Messages;
import terminus.common.TerminusLogger;
import terminus.exception.InvalidArgumentException;
import terminus.exception.InvalidCommandException;
import terminus.module.ModuleManager;
import terminus.parser.CommandParser;
import terminus.parser.MainCommandParser;
import terminus.storage.ModuleStorage;
import terminus.ui.Ui;

public class Terminus {

    public static final String[] INVALID_JSON_MESSAGE = {
        "Invalid file data detected.",
        "TermiNUS will still run, but the file will be overwritten when the next command is ran.",
        "To save your current file, close your terminal (do not run exit).",
        "Otherwise, you can continue using the program :)"
    };
    private Ui ui;
    private CommandParser parser;
    private String workspace;

    private ModuleStorage moduleStorage;
    private ModuleManager moduleManager;

    private static final Path DATA_DIRECTORY = Path.of(System.getProperty("user.dir"), "data");
    private static final String MAIN_JSON = "main.json";

    /**
     * Enters the main entry-point for the terminus.Terminus application.
     */
    public static void main(String[] args) {
        new Terminus().run();
    }

    /**
     * Starts the program.
     */
    public void run() {
        start();
        runCommandsUntilExit();
        exit();
    }

    private void start() {
        try {
            TerminusLogger.initializeLogger();
            TerminusLogger.info("Starting Terminus...");
            this.ui = new Ui();
            this.parser = MainCommandParser.getInstance();
            this.workspace = "";
            this.moduleStorage = ModuleStorage.getInstance();
            this.moduleStorage.init(DATA_DIRECTORY.resolve(MAIN_JSON));
            TerminusLogger.info("Loading file...");
            this.moduleManager = moduleStorage.loadFile();
        } catch (IOException e) {
            TerminusLogger.warning("File loading has failed.", e.fillInStackTrace());
            handleIoException(e);
        } catch (JsonSyntaxException | JsonIOException e) {
            TerminusLogger.severe("Invalid file data detected!", e.fillInStackTrace());
            ui.printSection(INVALID_JSON_MESSAGE);
        } finally {
            if (this.moduleManager == null) {
                TerminusLogger.warning("File not found.");
                TerminusLogger.warning("Creating new NusModule instance...");
                this.moduleManager = new ModuleManager();
            } else {
                TerminusLogger.info("File loaded.");
            }
            this.ui.printParserBanner(this.parser, this.moduleManager);
        }
        TerminusLogger.info("Terminus has started.");
    }

    private void runCommandsUntilExit() {
        while (true) {
            assert workspace != null : "Workspace should always have a value";
            String input = ui.requestCommand(workspace);
            TerminusLogger.debug("User entered: " + input);
            assert input != null : "Input should not be null.";

            Command currentCommand;
            try {
                currentCommand = parser.parseCommand(input);
                CommandResult result = currentCommand.execute(ui, moduleManager);

                boolean isExitCommand = result.isOk() && result.isExit();
                boolean isWorkspaceCommand = result.isOk() && result.getAdditionalData() != null;
                if (isExitCommand) {
                    break;
                } else if (isWorkspaceCommand) {
                    parser = result.getAdditionalData();
                    assert parser != null : "commandParser is not null";
                    workspace = parser.getWorkspace();
                    ui.printParserBanner(parser, moduleManager);
                }
                TerminusLogger.info("Saving data into file...");
                this.moduleStorage.saveFile(moduleManager);
                TerminusLogger.info("Save completed.");
            } catch (InvalidCommandException e) {
                TerminusLogger.warning("Invalid input provided: " + input, e.fillInStackTrace());
                ui.printSection(e.getMessage());
            } catch (InvalidArgumentException e) {
                TerminusLogger.warning("Invalid input provided: " + input, e.fillInStackTrace());

                // Check if the exception specified a correct command format for the user to follow.
                if (e.getFormat() != null) {
                    // Print the format of the command along with the error message to the user.
                    ui.printSection(e.getMessage(),
                            String.format(Messages.INVALID_ARGUMENT_FORMAT_MESSAGE, e.getFormat()));
                } else {
                    ui.printSection(e.getMessage());
                }
            } catch (IOException e) {
                TerminusLogger.warning("File saving has failed.");
                handleIoException(e);
            }
        }
    }

    private void handleIoException(IOException e) {
        TerminusLogger.severe("Save file is inaccessible.");
        TerminusLogger.severe(e.getMessage(), e.getCause());
        ui.printSection(
                String.format(Messages.ERROR_MESSAGE_FILE, e.getMessage()),
                "TermiNUS may still run, but your changes may not be saved.",
                "Check 'terminus.log' for more information."
        );
    }

    private void exit() {
        TerminusLogger.info("Saving data into file...");
        try {
            this.moduleStorage.saveFile(moduleManager);
            this.moduleStorage.saveAllNotes(moduleManager);
            TerminusLogger.info("Save completed.");
        } catch (IOException e) {
            TerminusLogger.warning("File saving has failed.");
            handleIoException(e);
        }
        this.ui.printExitMessage();
    }
}
