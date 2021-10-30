package terminus.storage;

import java.nio.file.Path;
import terminus.common.Messages;
import terminus.exception.InvalidFileException;

/**
 * FolderStorage class to handle Module folder storage operations.
 */
public class FolderStorage extends Storage {

    private Path baseDirectory;

    public FolderStorage(Path baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    /**
     * Executes the specified operation with the given arguments.
     *
     * @param module The new module name.
     * @param deletedModule The old module or old module name.
     * @param action The operation type to determine which operation to execute.
     * @throws InvalidFileException when any file I/O operations has error.
     */
    public void execute(String module, String deletedModule, StorageActionEnum action) throws InvalidFileException {
        switch (action) {
        case CREATE:
            createModuleFolder(module);
            break;
        case DELETE:
            deleteModuleFolder(deletedModule);
            break;
        case UPDATE:
            renameModuleFolder(module, deletedModule);
            break;
        default:
            throw new InvalidFileException(Messages.ERROR_STORAGE_INVALID_TYPE);
        }
    }

    /**
     * Creates a folder with the given module name.
     *
     * @param module The name of the folder to be created.
     * @throws InvalidFileException when any file I/O operations has error.
     */
    private void createModuleFolder(String module) throws InvalidFileException {
        Path moduleFolder = getAppendPath(baseDirectory, module);
        try {
            delete(moduleFolder);
            createFolder(moduleFolder);
        } catch (InvalidFileException e) {
            throw new InvalidFileException(String.format(Messages.ERROR_STORAGE_CREATE_FOLDER, moduleFolder));
        }

    }

    /**
     * Deletes a folder with the given name.
     *
     * @param deletedModule The name of the folder to be deleted.
     * @throws InvalidFileException when any file I/O operations has error.
     */
    private void deleteModuleFolder(String deletedModule) throws InvalidFileException {
        Path moduleFolder = getAppendPath(baseDirectory, deletedModule);
        delete(moduleFolder);
    }

    /**
     * Rename an existing folder to the given new name.
     *
     * @param module The new name for the folder.
     * @param deletedModule The name of the folder to be renamed.
     * @throws InvalidFileException when any file I/O operations has error.
     */
    private void renameModuleFolder(String module, String deletedModule) throws InvalidFileException {
        Path newModuleFolder = getAppendPath(baseDirectory, module);
        Path moduleFolder = getAppendPath(baseDirectory, deletedModule);
        renameFolder(moduleFolder, newModuleFolder);
    }
}
