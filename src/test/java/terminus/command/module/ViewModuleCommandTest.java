package terminus.command.module;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.itextpdf.text.DocumentException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import terminus.command.Command;
import terminus.command.CommandResult;
import terminus.exception.InvalidArgumentException;
import terminus.exception.InvalidCommandException;
import terminus.module.ModuleManager;
import terminus.parser.ModuleCommandParser;
import terminus.ui.Ui;

public class ViewModuleCommandTest {

    private static final String tempModule = "test";
    private ModuleCommandParser commandParser;
    private ModuleManager moduleManager;
    private Ui ui;

    @BeforeEach
    void setUp() {
        this.moduleManager = new ModuleManager();
        this.commandParser = ModuleCommandParser.getInstance();
        moduleManager.setModule(tempModule);
        this.ui = new Ui();
    }

    @Test
    void execute_viewModule_success()
            throws InvalidArgumentException, InvalidCommandException, IOException, DocumentException {
        Command cmd = commandParser.parseCommand("view");
        CommandResult cmdResult = cmd.execute(ui, moduleManager);
        assertTrue(cmdResult.isOk());
    }
}
