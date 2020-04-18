import GreenhouseDataModels.AlternativeOneGreenhouseData;
import GreenhouseDataModels.GreenhouseData;
import GreenhouseDataModels.StandardGreenhouseData;

public class GeneratorThread extends Thread {
    private int greenhouseId;
    private int greenhouseType;
    private boolean runThread;

    public GeneratorThread(int id, int alternative) {
        greenhouseId = id;
        greenhouseType = alternative;
        runThread = true;
    }

    public void run() {
        System.out.println( "Generator-Thread started with Greenhouse-ID " + greenhouseId + " and Type "  + greenhouseType);
        GreenhouseData greenhouseData;
        switch (greenhouseType) {
            case 1:
                greenhouseData = new StandardGreenhouseData(greenhouseId);
                break;
            case 2:
                greenhouseData = new AlternativeOneGreenhouseData(greenhouseId);
            default:
                greenhouseData = new StandardGreenhouseData(greenhouseId);
        }
        while(runThread) {

        }
    }

    public void stopExecution() {
        runThread = false;
    }
}
