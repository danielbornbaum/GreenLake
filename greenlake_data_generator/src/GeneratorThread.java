import GreenhouseDataModels.*;

import java.util.Calendar;
import java.util.Date;

public class GeneratorThread extends Thread {
    private int greenhouseId;
    private int greenhouseType;
    private boolean runThread;

    private GeneratorMonth january;
    private GeneratorMonth february;
    private GeneratorMonth march;
    private GeneratorMonth april;
    private GeneratorMonth may;
    private GeneratorMonth june;
    private GeneratorMonth july;
    private GeneratorMonth august;
    private GeneratorMonth september;
    private GeneratorMonth october;
    private GeneratorMonth november;
    private GeneratorMonth december;

    public GeneratorThread(int id, int alternative) {
        greenhouseId = id;
        greenhouseType = alternative;
        runThread = true;
        initialize();
    }

    public void run() {
        System.out.println( "Generator-Thread started with Greenhouse-ID " + greenhouseId + " and Type "  + greenhouseType);
        GreenhouseData greenhouseData;
        switch (greenhouseType) {
            case 1:
                greenhouseData = new StandardGreenhouseData(greenhouseId);
            case 2:
                greenhouseData = new AlternativeOneGreenhouseData(greenhouseId);
                break;
            default:
                greenhouseData = new StandardGreenhouseData(greenhouseId);
                break;
        }

        //TODO neuesten Datensatz von gewähltem Typ abrufen und an Folgetag weitermachen
        Date currentDate = new Date();//TODO replace with last date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.MINUTE, 1);

        int currentMonthNumber;
        int oldMonthNumber = 0;
        long timestamp;
        GeneratorMonth currentMonth = january;
        while(runThread) {
            currentMonthNumber = calendar.get(Calendar.MONTH);
            if(oldMonthNumber != currentMonthNumber) {
                switch (currentMonthNumber) {
                    case 1:
                        currentMonth = january;
                        break;
                    case 2:
                        currentMonth = february;
                        break;
                    case 3:
                        currentMonth = march;
                        break;
                    case 4:
                        currentMonth = april;
                        break;
                    case 5:
                        currentMonth = may;
                        break;
                    case 6:
                        currentMonth = june;
                        break;
                    case 7:
                        currentMonth = july;
                        break;
                    case 8:
                        currentMonth = august;
                        break;
                    case 9:
                        currentMonth = september;
                        break;
                    case 10:
                        currentMonth = october;
                        break;
                    case 11:
                        currentMonth = november;
                        break;
                    case 12:
                        currentMonth = december;
                        break;
                }
                oldMonthNumber = currentMonthNumber;
            }
            timestamp = calendar.getTimeInMillis();
            greenhouseData = GreenhouseData.generateNewData(greenhouseData, currentMonth, timestamp);
            //TODO save new data in lake
            calendar.add(Calendar.MINUTE, 1);
        }
    }

    public void stopExecution() {
        runThread = false;
    }

    private void initialize() {
        january = new GeneratorMonth(Season.WINTER, -2, -15, 4, 15, 9, (float) 1.5, 12, 85);
        february = new GeneratorMonth(Season.WINTER, -2, -15, 6, 17, 10, (float) 2.8, 9, 82);
        march = new GeneratorMonth(Season.SPRING, 1, -5, 10, 20, 12, (float) 4.2, 10, 79);
        april = new GeneratorMonth(Season.SPRING, 3, -5, 14, 25, (float) 13.5, (float) 6.3, 10, 74);
        may = new GeneratorMonth(Season.SPRING, 7, 0, 19, 25, 15, (float) 6.9, 12, 71);
        june = new GeneratorMonth(Season.SUMMER, 11, 5, 22, 35, 16, (float) 7.4, 11, 72);
        july = new GeneratorMonth(Season.SUMMER, 13, 5, 24, 35, (float) 15.5, (float) 7.2, 11, 71);
        august = new GeneratorMonth(Season.SUMMER, 12, 5, 25, 35, (float) 14.5, (float) 6.8, 10, 74);
        september = new GeneratorMonth(Season.FALL, 9, 0, 20, 25, (float) 12.5, (float) 5.3, 8, 79);
        october = new GeneratorMonth(Season.FALL, 6, -5, 15, 20, 11, (float) 3.6, 10,82);
        november = new GeneratorMonth(Season.FALL, 1, -10, 8, 17, (float) 9.5, (float) 1.8, 11, 84);
        december = new GeneratorMonth(Season.WINTER, -1, -15, 4, 15, (float) 8.5, (float) 1.2, 12, 85);
    }
}
