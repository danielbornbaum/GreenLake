package rest.services;

import org.json.JSONObject;
import settings.SettingsManager;
import setup.HadoopSetupBean;
import setup.KafkaSetupBean;
import setup.SetupBean;
import setup.SetupManager;
import util.HTTPStatusCodes;
import util.LoggedClientCompatibleException;
import util.RestRequestManager;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Deals with the setup of this application
 */
@Path("/setup")
public class SetupService
{
    private static final Logger LOGGER = Logger.getLogger(SetupService.class.getName());
    private JSONObject config = new JSONObject();

    /**
     * @return whether the setup was performed based on the existence a valid configuration file
     */
    @GET
    @Path("/status")
    public Response status()
    {
        return new RestRequestManager()
                .execute(restRequestManager -> {
                    try
                    {
                        restRequestManager
                                .setMessage(new JSONObject().put("performed", SetupManager.isPerformed()));
                    }
                    catch (IOException | LoggedClientCompatibleException e)
                    {
                        if (e instanceof LoggedClientCompatibleException)
                        {
                            restRequestManager.setCustomError((LoggedClientCompatibleException) e);
                        }
                        restRequestManager.setCustomError(new LoggedClientCompatibleException(e, LOGGER));
                    }
                })
                .generateResponse();
    }

    /**
     * Interface to install Apache Kafka
     *
     * @param parameters a json request with parameter 'path'
     * @return code 200 or error code and error message
     */
    @PUT
    @Path("/installKafka")
    public Response installKafka(String parameters)
    {
        return installComponent(KafkaSetupBean.class, parameters);
    }

    /**
     * Interface to install Apache Hadoop
     *
     * @param parameters a json request with parameter 'path'
     * @return code 200 or error code and error message
     */
    @PUT
    @Path("/installHadoop")
    public Response installHadoop(String parameters)
    {
        return installComponent(HadoopSetupBean.class, parameters);
    }

    /**
     * Interface to get the kafka setup progress
     *
     * @return Kakfa setup progress in format {progress: int} (percentage) or error code and message
     */
    @GET
    @Path("/kafkaSetupProgress")
    public Response kafkaStatus()
    {
        return getProgress(KafkaSetupBean.class);
    }

    /**
     * Interface to get the kafka setup progress
     *
     * @return Haoop setup progress in format {progress: int}  (percentage) or error code and message
     */
    @GET
    @Path("/hadoopSetupProgress")
    public Response hadoopStatus()
    {
        return getProgress(HadoopSetupBean.class);
    }

    /**
     * Interface to validate Kafka at a given location
     *
     * @param params json request with key path, where to validate kafka at
     * @return {present: boolean} or error code and message
     */
    @POST
    @Path("/validateKafkaAtLocation")
    public Response validateKafkaAtLocation(String params)
    {
        return new RestRequestManager()
                .setParameters(params)
                .assertKeys(new String[]{"path"})
                .execute(restRequestManager -> {
                    boolean present = false;
                    try
                    {
                        present = KafkaSetupBean.getInstance()
                                .validateFolder(restRequestManager.getString("path"));
                    }
                    catch (LoggedClientCompatibleException e)
                    {
                        restRequestManager.setCustomError(e);
                        return;
                    }

                    String path = restRequestManager.getString("path").replace("\\", "/");
                    SettingsManager.getInstance().setSetting("pathToKafka", path, true);
                    restRequestManager.setMessage(new JSONObject().put("present", present));
                })
                .generateResponse();
    }

    /**
     * Interface to validate hadoop at a given location
     *
     * @param params json request with key path, where to validate kafka at
     * @return {present: boolean} or error code and message
     */
    @POST
    @Path("/validateHadoopAtLocation")
    public Response validateHadoopAtLocation(String params)
    {
        return new RestRequestManager()
                .setParameters(params)
                .assertKeys(new String[]{"path"})
                .execute(restRequestManager -> {
                    boolean present = false;
                    try
                    {
                        present = HadoopSetupBean.getInstance()
                                .validateFolder(restRequestManager.getString("path"));
                    }
                    catch (LoggedClientCompatibleException e)
                    {
                        restRequestManager.setCustomError(new LoggedClientCompatibleException(e, LOGGER));
                        return;
                    }

                    String path = restRequestManager.getString("path").replace("\\", "/");
                    SettingsManager.getInstance().setSetting("pathToHadoop", path, true);
                    restRequestManager.setMessage(new JSONObject().put("present", present));
                })
                .generateResponse();
    }

    /**
     * Interface to set the Kafka and Zookeeper properties for this application, the old file is renamed and the
     * previous configuration logged
     *
     * @param params properties in json format containing {property.name = property-value} for kafka and
     *         zookeeper
     * @return success code and empty message body or error code and error message
     */
    @POST
    @Path("/setKafkaProperties")
    public Response setKafkaProperties(String params)
    {
        if (SettingsManager.getInstance().hasSetting("pathToKafka"))
        {
            String pathToKafkaConfigFile = SettingsManager.getInstance().getSetting("pathToKafka")
                    .concat("/config/server.properties");
            String pathToZookeeperConfigFile = SettingsManager.getInstance().getSetting("pathToKafka")
                    .concat("/config/zookeeper.properties");
            return setKafkaProperties(params, new String[]{pathToKafkaConfigFile, pathToZookeeperConfigFile});
        }
        else
        {
            return new RestRequestManager()
                    .setCustomError(new LoggedClientCompatibleException(
                            new IllegalStateException(
                                    "A Client requested to set Kafkas properties, but the path to Kafka is not set yet"),
                            "Der Pfad zu Kafka wurde noch nicht gesetzt",
                            HTTPStatusCodes.CLIENT_ISSUES.FORBIDDEN, LOGGER, Level.FINE))
                    .generateResponse();
        }
    }

    /**
     * Interface to finish the setup and persisting the configuration
     *
     * @return 200 and an empty response body or an error code and message
     */
    @POST
    @Path("/finish")
    public Response finish()
    {
        if (!SettingsManager.getInstance().hasSetting("pathToKafka"))
        {
            return new RestRequestManager()
                    .setCustomError(new LoggedClientCompatibleException(
                            new IllegalStateException(
                                    "A Client requested to finish the setup, but the path to Kafka is not set yet"),
                            "Der Pfad zu Kafka wurde noch nicht gesetzt",
                            HTTPStatusCodes.CLIENT_ISSUES.FORBIDDEN, LOGGER, Level.FINE))
                    .generateResponse();
        }

        if (!SettingsManager.getInstance().hasSetting("pathToHadoop"))
        {
            return new RestRequestManager()
                    .setCustomError(new LoggedClientCompatibleException(
                            new IllegalStateException(
                                    "A Client requested to finish the setup, but the path to Hadoop is not set yet"),
                            "Der Pfad zu Hadoop wurde noch nicht gesetzt",
                            HTTPStatusCodes.CLIENT_ISSUES.FORBIDDEN, LOGGER, Level.FINE))
                    .generateResponse();
        }

        return new RestRequestManager()
                .execute(restRequestManager ->
                         {
                             try
                             {
                                 SettingsManager.getInstance().persistSettings();
                             }
                             catch (IOException exception)
                             {
                                 restRequestManager
                                         .setCustomError(new LoggedClientCompatibleException(exception, LOGGER));
                             }

                         }).generateResponse();
    }

    /**
     * helper method to install components based on their beans
     *
     * @param component, bean which is capable of installing the component
     * @param parameters parameters containing the path where to install the component to as json
     * @return code 200 and empty message body or error code and message
     */
    private Response installComponent(Class<?> component, String parameters)
    {
        return new RestRequestManager()
                .setParameters(parameters)
                .assertKeys(new String[]{"path"})
                .execute(restRequestManager -> {
                    File dir = new File(restRequestManager.getString("path"));

                    if (dir.exists())
                    {
                        if (!dir.isDirectory())
                        {
                            restRequestManager.setCustomError(new LoggedClientCompatibleException(
                                    new IllegalStateException(
                                            String.format(
                                                    "A client requested an installation with %s. But %s is not a valid directory",
                                                    component.getSimpleName(), dir.getAbsolutePath())),
                                    "Der angegebene Pfad ist kein Verzeichnis",
                                    HTTPStatusCodes.CLIENT_ISSUES.BAD_REQUEST, LOGGER, Level.FINE));
                            return;
                        }

                        String[] folderList = dir.list();

                        if (folderList != null)
                        {
                            if (folderList.length > 0)
                            {
                                restRequestManager.setCustomError(new LoggedClientCompatibleException(
                                        new IllegalStateException(
                                                String.format(
                                                        "A client requested an installation with %s. But %s is not empty",
                                                        component.getSimpleName(), dir.getAbsolutePath())),
                                        "Das angegebene Verzeichnis ist nicht leer\"",
                                        HTTPStatusCodes.CLIENT_ISSUES.CONFLICT, LOGGER, Level.FINE));
                                return;
                            }
                        }
                    }

                    try
                    {
                        getBeanFromName(component).install(restRequestManager.getString("path"));
                    }
                    catch (IOException | LoggedClientCompatibleException e)
                    {
                        if (e instanceof LoggedClientCompatibleException)
                        {
                            restRequestManager.setCustomError((LoggedClientCompatibleException) e);
                            return;
                        }

                        restRequestManager.setCustomError(new LoggedClientCompatibleException(e, LOGGER));
                    }
                })
                .generateResponse();
    }

    /**
     * Helper method to get progress of a component installation by its bean class
     *
     * @param component bean to get the progress for
     * @return code 200 and {progress: int} (percentage) or error code and error message
     */
    private Response getProgress(Class<?> component)
    {
        return new RestRequestManager()
                .execute(restRequestManager -> {
                    int progress = 0;
                    try
                    {
                        progress = getBeanFromName(component).getProgress();
                    }
                    catch (LoggedClientCompatibleException e)
                    {
                        restRequestManager.setCustomError(e);
                        return;
                    }

                    if (progress == -1)
                    {
                        restRequestManager.setCustomError(new LoggedClientCompatibleException(
                                new IllegalStateException(
                                        String.format(
                                                "A client requested to get the progress of %s, however, the installation was not started yet",
                                                component.getSimpleName())),
                                "Das Setup wurde noch nicht gestartet. Es kann kein Fortschritt angezeigt werden.\"",
                                HTTPStatusCodes.CLIENT_ISSUES.FORBIDDEN, LOGGER, Level.FINE));
                    }
                    else
                    {
                        restRequestManager.setMessage(new JSONObject().put("progress", progress));
                    }
                })
                .generateResponse();
    }

    /**
     * Translates a class name to a bean instance
     *
     * @param component bean name to get the instance for
     * @return bean instance
     * @throws LoggedClientCompatibleException if bean is not implemented or no installation bean
     */
    private SetupBean getBeanFromName(Class<?> component) throws LoggedClientCompatibleException
    {
        SetupBean bean;

        switch (component.getSimpleName())
        {
            case "KafkaSetupBean":
                bean = KafkaSetupBean.getInstance();
                break;
            case "HadoopSetupBean":
                bean = HadoopSetupBean.getInstance();
                break;
            default:
                throw new LoggedClientCompatibleException(
                        new UnsupportedOperationException(
                                String.format("There is no installer bean for this component: %s",
                                              component.getSimpleName())),
                        "Eine Implementierung auf serverseite fehlt noch.",
                        HTTPStatusCodes.SERVER_ISSUES.NOT_IMPLEMENTED, LOGGER);
        }

        return bean;
    }

    /**
     * Helper method for setting the Kafka properties to the given files
     *
     * @param params settings to set for the file, if it contains the setting
     * @param pathsToConfigFiles files to try to overwrite the settings in
     * @return code 200 and empty response body or error code and message
     */
    private Response setKafkaProperties(String params, String[] pathsToConfigFiles)
    {
        RestRequestManager manager = new RestRequestManager()
                .setParameters(params)
                .assertKeys(new String[]{"config"});


        for (String pathToConfigFile : pathsToConfigFiles)
        {
            manager.execute(restRequestManager -> {
                File configFile = new File(pathToConfigFile);

                if (configFile.exists())
                {
                    try
                    {
                        KafkaSetupBean.getInstance()
                                .setProperties(new JSONObject(params).getJSONObject("config"), configFile);
                    }
                    catch (IOException e)
                    {

                        restRequestManager.setCustomError(new LoggedClientCompatibleException(e,
                                                                                              "Konnte nicht auf die Konfigurationsdatei zugreifen",
                                                                                              HTTPStatusCodes.SERVER_ISSUES.INTERNAL_SERVER_ERROR,
                                                                                              LOGGER));
                    }
                }
                else
                {
                    restRequestManager.setCustomError(new LoggedClientCompatibleException(
                            new IllegalStateException("Could not access the config file. It does not exist at path"
                                                              .concat(pathToConfigFile)),
                            "Konnte nicht auf die Konfigurationsdatei zugreifen. Sie schein nicht zu existieren.",
                            HTTPStatusCodes.SERVER_ISSUES.INTERNAL_SERVER_ERROR,
                            LOGGER));
                }
            });
        }
        return manager.generateResponse();
    }
}
