package setup;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import settings.SettingsManager;
import util.HTTPStatusCodes;
import util.LoggedClientCompatibleException;

import javax.ejb.Stateless;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to set a required component up
 */
@Stateless
public abstract class SetupBean
{
    private static final Logger LOGGER = Logger.getLogger(SetupBean.class.getName());
    private int progress = -1;

    /**
     * Requires install with parameter string in subclasses
     *
     * @param path path to install the component to
     * @throws IOException possibly thrown by the call of the install method inside this class
     * @throws LoggedClientCompatibleException possibly thrown by the call of the install method inside this
     *         class
     */
    public abstract void install(String path) throws IOException, LoggedClientCompatibleException;

    /**
     * requires the subclasses to have a getNameOfInstallationGoal method, that can be used for logging and stuff
     *
     * @return name of the installation goal
     */
    protected abstract String getNameOfInstallationGoal();

    /**
     * Installs a given component
     *
     * @param path where to install the component to
     * @param strUrl where to download the component from
     * @throws IOException if access to the given path is not possible or the download stream fails
     * @throws LoggedClientCompatibleException possibly thrown when converting the given path to an absolute
     *         path, see method toAbsolutePath for reference
     */
    protected void install(String path, String strUrl) throws IOException, LoggedClientCompatibleException
    {
        LOGGER.fine(String.format("\n\n\nTrying to install %s to path %s\n\n\n", getNameOfInstallationGoal(), path));
        path = toAbsolutePath(path).concat("/");

        if (progress > -1)
        {
            return;
        }

        URL url = new URL(strUrl);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        if (connection.getResponseCode() >= 200 && connection.getResponseCode() < 300)
        {
            int contentLength = connection.getContentLength();

            if (contentLength <= 0)
            {
                throw new IOException(String.format("Größe der %s-Installation war %d. Keine Daten.",
                                                    getNameOfInstallationGoal(), contentLength));
            }

            InputStream webInputStream = connection.getInputStream();

            String pathToDownloadFile = path.concat("download.tgz");

            if (!new File(path).exists())
            {
                File downloadFile = new File(pathToDownloadFile);
                boolean mkdirs = downloadFile.getParentFile().mkdirs();

                if (!mkdirs)
                {
                    throw new IOException(
                            String.format("Konnte kein Verzeichnis erstellen, vielleicht ist der Pfad '%s' invalide?",
                                          path));
                }

                boolean created = downloadFile.createNewFile();

                if (!created)
                {
                    throw new IOException(String.format("Konnte die Datei '%s' nicht erstellen.", pathToDownloadFile));
                }
            }

            FileOutputStream outputStream = new FileOutputStream(pathToDownloadFile);

            int bytesRead = -1;
            long bytesReadTotal = 0;
            byte[] buffer = new byte[4096];
            while ((bytesRead = webInputStream.read(buffer)) != -1)
            {
                bytesReadTotal += bytesRead;
                double bytesPercentage = ((double) bytesReadTotal / contentLength) * 99;
                progress = (int) Math.round(bytesPercentage);
                outputStream.write(buffer, 0, bytesRead);
            }

            webInputStream.close();
            outputStream.close();

            File downloadTar = new File(pathToDownloadFile);

            Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
            archiver.extract(downloadTar, new File(path));

            if (downloadTar.delete())
            {
                LOGGER.info(String.format("Deleted tar file for %ss installation", getNameOfInstallationGoal()));
            }
            else
            {
                LOGGER.warning(
                        String.format("Could not delete tar file for %ss installation", getNameOfInstallationGoal()));
            }

            File installationSubDir = Objects
                    .requireNonNull(new File(path).listFiles(File::isDirectory))[0];
            File[] installationFiles = Objects.requireNonNull(installationSubDir.listFiles());

            for (File file : installationFiles)
            {
                if (!file.renameTo(new File(path.concat(file.getName()))))
                {
                    throw new IOException(
                            String.format("Dateien für %ss Installation konnten nicht verschoben werden.",
                                          getNameOfInstallationGoal()));
                }
            }

            if (!installationSubDir.delete())
            {
                LOGGER.warning("Could not delete unnecessary and empty subdirectory with version");
            }
            progress = 100;
        }
        else
        {
            throw new IOException(String.format("Response code war %d", connection.getResponseCode()));
        }
    }

    /**
     * validate an installation of a folder by comparing it to a hash
     *
     * @param pathToFolder path to folder to validate
     * @param hashSetting name of the setting, that contains the expected has
     * @return whether the generated and expected hash were equal
     * @throws LoggedClientCompatibleException thrown when converting path to an absolute path when hashing. See
     *         methods hashFoldersRecursively and toAbsolutePath for reference
     */
    public boolean validateFolder(String pathToFolder, String hashSetting) throws LoggedClientCompatibleException
    {
        //absolute path is generated in hashFoldersRecursively(pathToFolder)
        String folderHash = hashFoldersRecursively(pathToFolder);
        LOGGER.log(Level.FINE,
                   String.format("\n\n\nValidating installation\n\tExpected hash: %s\n\tActual hash: %s\n\n\n",
                                 SettingsManager.getInstance().getSetting(hashSetting), folderHash));
        return SettingsManager.getInstance().getSetting(hashSetting).equals(folderHash);
    }

    /**
     * @return progress of the installation
     */
    public int getProgress()
    {
        if (progress == 100)
        {
            progress = -1;
            return 100;
        }
        return progress;
    }

    /**
     * Sets properties from a given JSONObject to a given file. Replacement is done by searching for
     * property.name=something and possibly replacing it, if the keys match
     *
     * @param properties to set if the keys exist
     * @param configFile file to set the properties in
     * @throws IOException when file can't be accessed
     */
    public void setProperties(JSONObject properties, File configFile) throws IOException
    {
        String fileContent = FileUtils.readFileToString(configFile, "UTF-8");

        LOGGER.info("\n\n---\n"
                            .concat("Replacing ")
                            .concat(getNameOfInstallationGoal()).concat(" Configuration. Old config:")
                            .concat("\n\n")
                            .concat(fileContent)
                            .concat("\n\n---\n"));

        for (String property : properties.keySet())
        {
            String pattern = property.concat("=.*\n");
            String replacement = property.concat("=").concat(String.valueOf(properties.get(property)))
                    .concat("\n");

            fileContent = fileContent.replaceAll(pattern, replacement);
        }

        FileUtils.writeStringToFile(configFile, fileContent, "UTF-8");
    }

    /**
     * Hashes folder and file names recursively in alphabetical order and generates a hash from them
     *
     * @param path path to generate the hash for
     * @return the hash as a String
     * @throws LoggedClientCompatibleException possibly thrown when converting path to an absolute path. See
     *         method toAbsolutePath for reference
     */
    private String hashFoldersRecursively(String path) throws LoggedClientCompatibleException
    {
        path = toAbsolutePath(path);
        StringBuilder hashBuilder = new StringBuilder();

        if (!new File(path).exists())
        {
            return "";
        }

        //alphabetically sorted files and folders
        List<File> dirs = Arrays.asList(Objects.requireNonNull(new File(path).listFiles(File::isDirectory)));
        dirs.sort(Comparator.comparing(File::getName));
        List<File> files = Arrays.asList(Objects.requireNonNull(new File(path).listFiles(File::isFile)));
        files.sort(Comparator.comparing(File::getName));

        for (File dir : dirs)
        {
            hashBuilder.append(hashFoldersRecursively(path.concat(dir.getName()).concat("/")));
            hashBuilder.append(dir.getName().hashCode());
        }

        for (File file : files)
        {
            hashBuilder.append(file.getName().hashCode());
        }

        return hashBuilder.toString();
    }

    /**
     * @param path path to convert to absolute path
     * @return absolute path
     * @throws LoggedClientCompatibleException if the path is invalid
     */
    String toAbsolutePath(String path) throws LoggedClientCompatibleException
    {
        String absolutePath = path.replace("\\", "/");

        try
        {
            Path pathObj = Paths.get(absolutePath);
            absolutePath = pathObj.toAbsolutePath().toString();
        }
        catch (InvalidPathException | NullPointerException exception)
        {
            throw new LoggedClientCompatibleException(exception, "Der Pfad ist nicht valide",
                                                      HTTPStatusCodes.CLIENT_ISSUES.BAD_REQUEST, LOGGER, Level.FINE);
        }

        absolutePath = absolutePath.replace("\\", "/");
        absolutePath = absolutePath.endsWith("/") ? absolutePath.substring(0, absolutePath.length() - 1) : absolutePath;

        return absolutePath;
    }
}
