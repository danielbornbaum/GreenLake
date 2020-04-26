package setup;

import java.io.IOException;

public class HadoopSetupBean extends SetupBean<HadoopSetupBean>
{
    private HadoopSetupBean()
    {
    }

    private static HadoopSetupBean instance;

    public static HadoopSetupBean getInstance()
    {
        if (instance == null)
        {
            instance = new HadoopSetupBean();
        }

        return instance;
    }

    @Override
    public void install(String path) throws IOException
    {
        super.install(path, "https://mirror.softaculous.com/apache/hadoop/common/hadoop-2.10.0/hadoop-2.10.0.tar.gz");
    }

    @Override
    protected String getNameOfInstallationGoal()
    {
        return "Hadoop";
    }
}
