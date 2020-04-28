import GreenhouseDataModels.*;
import javafx.util.Pair;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
        IGreenhouseData greenhouseData;
        int secondInterval = 300;
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
        float lastTemperatureIn = 20;
        float lastTemperatureOut = 5;
        float lastHumidityIn = 50;
        float lastHumidityOut = 80;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_WEEK_IN_MONTH, 1);

        int currentMonthNumber;
        int oldMonthNumber = 0;
        int monthRainDays = 0;
        Date date;
        GeneratorMonth currentMonth = january;
        Pair<List<IGreenhouseData>, Integer> result;
        while(runThread) {
            //TODO last... Werte überschreiben
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
                monthRainDays = 0;
            }
            date = calendar.getTime();
            result = greenhouseData.generateNewDay(secondInterval, monthRainDays, currentMonth, date, lastTemperatureIn, lastTemperatureOut, lastHumidityIn, lastHumidityOut);
            monthRainDays = result.getValue();
            //TODO save new data in lake
            calendar.add(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
        }
    }

    public void stopExecution() {
        runThread = false;
    }

    private void initialize() {
        january = new GeneratorMonth(Season.WINTER, -2, -15, 4, 15, LocalTime.of(7,45), LocalTime.of(16, 45), (float) 1.5, 12, 85);
        february = new GeneratorMonth(Season.WINTER, -2, -15, 6, 17, LocalTime.of(7,20), LocalTime.of(17, 20), (float) 2.8, 9, 82);
        march = new GeneratorMonth(Season.SPRING, 1, -5, 10, 20, LocalTime.of(6, 15), LocalTime.of(18, 15), (float) 4.2, 10, 79);
        april = new GeneratorMonth(Season.SPRING, 3, -5, 14, 25, LocalTime.of(6, 10), LocalTime.of(19, 40), (float) 6.3, 10, 74);
        may = new GeneratorMonth(Season.SPRING, 7, 0, 19, 25, LocalTime.of(5,20), LocalTime.of(20, 20), (float) 6.9, 12, 71);
        june = new GeneratorMonth(Season.SUMMER, 11, 5, 22, 35, LocalTime.of(5,00), LocalTime.of(21, 00), (float) 7.4, 11, 72);
        july = new GeneratorMonth(Season.SUMMER, 13, 5, 24, 35, LocalTime.of(5,20), LocalTime.of(20, 50), (float) 7.2, 11, 71);
        august = new GeneratorMonth(Season.SUMMER, 12, 5, 25, 35, LocalTime.of(5,50), LocalTime.of(19, 50), (float) 6.8, 10, 74);
        september = new GeneratorMonth(Season.FALL, 9, 0, 20, 25, LocalTime.of(6,40), LocalTime.of(19, 10), (float) 5.3, 8, 79);
        october = new GeneratorMonth(Season.FALL, 6, -5, 15, 20, LocalTime.of(7, 20), LocalTime.of(18, 20), (float) 3.6, 10,82);
        november = new GeneratorMonth(Season.FALL, 1, -10, 8, 17, LocalTime.of(7, 20), LocalTime.of(16, 50), (float) 1.8, 11, 84);
        december = new GeneratorMonth(Season.WINTER, -1, -15, 4, 15, LocalTime.of(7,50), LocalTime.of(16, 20), (float) 1.2, 12, 85);
    }
}
