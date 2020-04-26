package setup;

import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

import javax.ejb.Stateless;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@Stateless
public abstract class SetupBean<T>
{
    private static final Logger LOGGER = Logger.getLogger(SetupBean.class.getName());
    private int progress = -1;

    public abstract void install(String path) throws IOException;

    protected abstract String getNameOfInstallationGoal();

    protected void install(String path, String strUrl) throws IOException
    {
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
                throw new IOException(String.format("Content length of %s download was %d", getNameOfInstallationGoal(),
                                                    contentLength));
            }

            InputStream webInputStream = connection.getInputStream();

            StringBuilder pathToDownload = new StringBuilder();
            pathToDownload.append(path);

            if (!pathToDownload.toString().endsWith("/"))
            {
                pathToDownload.append("/");
            }

            String pathToDownloadFile = pathToDownload.toString().concat("download.tgz");

            File downloadFile = new File(pathToDownloadFile);
            boolean mkdirs = downloadFile.getParentFile().mkdirs();
            boolean created = downloadFile.createNewFile();

            if (!created || !mkdirs)
            {
                throw new IOException(
                        "Could not create file. Maybe it already exists and is not empty or you path is invalid.");
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
            archiver.extract(downloadTar, new File(pathToDownload.toString()));

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
                    .requireNonNull(new File(pathToDownload.toString()).listFiles(File::isDirectory))[0];
            File[] installationFiles = Objects.requireNonNull(installationSubDir.listFiles());

            for (File file : installationFiles)
            {
                if (!file.renameTo(new File(pathToDownload.toString().concat(file.getName()))))
                {
                    throw new IOException(
                            String.format("Could not move files for %ss installation", getNameOfInstallationGoal()));
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
            throw new IOException(String.format("Response Code was %d", connection.getResponseCode()));
        }
    }

    public boolean validateFolder(String pathToFolder, String pathToHashFile)
    {
        hashFoldersRecursively(pathToFolder);
        return false;
    }

    public int getProgress()
    {
        if (progress == 100)
        {
            progress = -1;
            return 100;
        }
        return progress;
    }

    private String hashFoldersRecursively(String path)
    {
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

        LOGGER.info("HASH CODE INCOMING: " + hashBuilder.toString());
        return hashBuilder.toString();
    }
}
