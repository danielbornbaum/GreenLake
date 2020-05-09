package GreenhouseDataModels;

import javafx.util.Pair;
import org.apache.kafka.common.protocol.types.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

public class StandardGreenhouseData extends GreenhouseData {
    final Logger logger = LoggerFactory.getLogger(StandardGreenhouseData.class);

    private int moistureSensValue3;
    private int moistureSensValue4;
    private Random random;

    public StandardGreenhouseData(int id) { setId(id); }

    public int getMoistureSensValue3() {
        return moistureSensValue3;
    }

    public int getMoistureSensValue4() {
        return moistureSensValue4;
    }

    public void setMoistureSensValue3(int moistureSensValue3) {
        this.moistureSensValue3 = moistureSensValue3;
    }

    public void setMoistureSensValue4(int moistureSensValue4) {
        this.moistureSensValue4 = moistureSensValue4;
    }

    public Pair<List<GreenhouseData>, Integer> generateNewDay(int secondInterval, int monthRainDays, GeneratorMonth month, GreenhouseData lastData) {
        Calendar calendar = (Calendar) lastData.getTime().clone();
        logger.info(String.format("Started generating data for Greenhouse %d at %d.%d.%d",
                this.getId(),
                calendar.get(Calendar.DAY_OF_MONTH),
                (calendar.get(Calendar.MONTH) + 1),
                calendar.get(Calendar.YEAR)));
        random = new Random();
        List<GreenhouseData> day = new ArrayList<>();
        int startId = 1;
        int monthDays = YearMonth.of(calendar.get(Calendar.YEAR), (calendar.get(Calendar.MONTH) + 1)).lengthOfMonth();
        int entryCount = (24 * 3600) / secondInterval;
        int morning = (10 * 3600) / secondInterval;
        int noon = (12 * 3600) / secondInterval;
        int afternoon = (16 * 3600) / secondInterval;
        int rainStartEntry = 0;
        int rainEndEntry = 0;
        int fogEndEntry = 0;
        int brightEntries = Math.round(month.intenseSunHours * 3600 / secondInterval);
        int leftNormalEntries = entryCount;
        boolean ventilate = false;
        boolean rain = false;
        boolean humidBeforeRain;
        int humidEntryBeforeRain = 0;
        boolean fog = false;
        float tempInside = 0;
        float tempOutside = 0;
        float humidityInside = 0;
        float humidityOutside = 0;
        float brightness = 0;
        float tempDifference = 0;
        boolean water1 = false;
        boolean water2 = false;
        boolean water3 = false;
        boolean water4 = false;

        //Generate special conditions (rain / fog)
        if(generateWeightedDecision(0.4) || monthDays - calendar.get(Calendar.DAY_OF_MONTH) == month.rainDays) {
            monthRainDays++;
            int rainEntries = (int) Math.round((1 + random.nextInt(9)) * 3600 / secondInterval);
            leftNormalEntries = leftNormalEntries - rainEntries;
            rainStartEntry = random.nextInt(entryCount - rainEntries);
            rainEndEntry = rainStartEntry + rainEntries - 1;
            if (month.season == Season.SUMMER && generateWeightedDecision(0.35) &&
                    rainStartEntry > (10 * 3600 / secondInterval)) {
                humidEntryBeforeRain = rainStartEntry - Math.round(random.nextInt(8) / 2 * 3600 / secondInterval);
            }
            else {
                humidEntryBeforeRain = 0;
            }
        }
        else {
            rainStartEntry = 0;
            rainEndEntry = 0;
            humidEntryBeforeRain = 0;
        }

        if (month.season == Season.FALL && generateWeightedDecision(0.84)) {
            int fogEntriesSinceSunrise = Math.round((1 + random.nextInt(3)) * 3600 / secondInterval);
            leftNormalEntries = leftNormalEntries - fogEntriesSinceSunrise;
            fogEndEntry = (int) Math.round((month.sunrise.getMinute() * 60 + month.sunrise.getHour() * 3600 + 3600 * Math.random() * 0.25) / secondInterval);
        }

        StandardGreenhouseData lastInstance = (StandardGreenhouseData) lastData;
        StandardGreenhouseData data = null;

        //Loop generating all other entries
        for (int entry = 1; entry <= entryCount; entry++) {

            //Set basic data
            data = new StandardGreenhouseData(startId);

            data.setTime((Calendar) lastInstance.getTime().clone());
            brightness = lastInstance.getBrightnessSensValue();
            tempOutside = lastInstance.getTempSensValue1();
            tempInside = lastInstance.getTempSensValue2();
            tempDifference = tempOutside - tempInside;
            humidityOutside = lastInstance.getHumiditySensValue1();
            humidityInside = lastInstance.getHumiditySensValue2();

            if (humidEntryBeforeRain > 0 && humidEntryBeforeRain <= entry && entry < rainStartEntry) {
                humidBeforeRain = true;
                rain = false;
            }
            else if (rainEndEntry > 0 && rainStartEntry <= entry && entry <= rainEndEntry) {
                rain = true;
                humidBeforeRain = false;
            }
            else {
                rain = false;
                humidBeforeRain = false;
            }
            if(entry < fogEndEntry) {
                fog = true;
            }
            else {
                fog = false;
            }

            //Set Time
            if (entry > 1) {
                calendar = data.getTime();
                calendar.setTimeInMillis(calendar.getTimeInMillis() + secondInterval * 1000);
                data.setTime(calendar);
            }
            LocalTime currentTime = LocalTime.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));


            //Set Moisture
            Pair tempPair = generateMoisture(lastInstance.getMoistureSensValue1(), water1);
            data.setMoistureSensValue1((int) tempPair.getKey());
            water1 = (boolean) tempPair.getValue();
            tempPair = generateMoisture(lastInstance.getMoistureSensValue2(), water2);
            data.setMoistureSensValue2((int) tempPair.getKey());
            water2 = (boolean) tempPair.getValue();
            tempPair = generateMoisture(lastInstance.getMoistureSensValue3(), water3);
            data.setMoistureSensValue3((int) tempPair.getKey());
            water3 = (boolean) tempPair.getValue();
            tempPair = generateMoisture(lastInstance.getMoistureSensValue4(), water4);
            data.setMoistureSensValue4((int) tempPair.getKey());
            water4 = (boolean) tempPair.getValue();

            //Set Brightness
            tempPair = generateBrightness(brightness, currentTime, month, entry, noon, rain, fog, leftNormalEntries, brightEntries);
            brightness = (float) tempPair.getKey();
            brightEntries = (int) tempPair.getValue();
            data.setBrightnessSensValue(brightness);

            //Set Humidity
            humidityOutside = generateHumidityOutside(fog, rain, humidBeforeRain, entry, humidEntryBeforeRain, morning, noon, afternoon, humidityOutside, month, brightness);
            data.setHumiditySensValue1(humidityOutside);

            tempPair = generateHumidityInside(humidityInside, ventilate, rain, tempInside);
            ventilate = (boolean) tempPair.getKey();
            humidityInside = (float) tempPair.getValue();
            data.setHumiditySensValue2(humidityInside);

            //Set Temperature
            tempOutside = generateTempOutside(tempOutside, month, rain, currentTime, fog, brightness);
            data.setTempSensValue1(tempOutside);

            tempInside = generateTempInside(tempInside, tempDifference, brightness, rain, ventilate);
            data.setTempSensValue2(tempInside);

            day.add(data);
            lastInstance = data;
            startId++;
            if(!fog && !rain) {
                leftNormalEntries--;
            }
        }

        logger.info("Generating day finished - returning result");
        return new Pair<>(day, monthRainDays);
    }

    private boolean generateWeightedDecision(double occurenceProbability) {
        double random = Math.random();
        if (random > occurenceProbability) {
            return false;
        }
        else {
            return true;
        }
    }

    private Pair<Integer, Boolean> generateMoisture(int lastMoisture, boolean water){
        if(lastMoisture < 70) {
            water = true;
        }
        else if(lastMoisture > 82 && water) {
            water = false;
        }
        if(water) {
            lastMoisture = lastMoisture + 4;
        }
        else if(generateWeightedDecision(0.5)){
            lastMoisture = lastMoisture - 2;
        }
        else {
            lastMoisture = lastMoisture - 1;
        }
        return new Pair<>(lastMoisture, water);
    }

    private float generateHumidityOutside(boolean fog, boolean rain, boolean humidBeforeRain, int entry, int humidEntryBeforeRain, int morning, int noon, int afternoon, float humidityOutside, GeneratorMonth month, float brightness) {
        if (fog) {
            humidityOutside = 95 + (float) Math.random() * 5;
        }
        else if (rain || (humidBeforeRain && entry >= humidEntryBeforeRain)) {
            if (humidityOutside < 70) {
                humidityOutside = humidityOutside + (float) Math.random() * 10;
            } else {
                humidityOutside = humidityOutside - 3 + (float) Math.random() * 6;
                if (humidityOutside < 70) {
                    humidityOutside = 70 + (float) Math.random();
                } else if (humidityOutside > 90) {
                    humidityOutside = 90 + (float) Math.random();
                }
            }
        }
        else if (month.season == Season.SUMMER && noon < entry && entry < afternoon) {
            if (humidityOutside > 55) {
                humidityOutside = humidityOutside - (float) Math.random() * 5;
            }
            else if (humidityOutside < 40) {
                humidityOutside = 40 + (float) Math.random();
            }
            else {
                humidityOutside = humidityOutside - 2 + (float) Math.random() * 4;
            }
        }
        else if (((month.season == Season.SPRING || month.season == Season.FALL) && entry < morning) || (month.season == Season.WINTER && brightness > 70)) {
            if (humidityOutside > 85) {
                humidityOutside = 84 + (float) Math.random();
            }
            else if (humidityOutside < 70) {
                humidityOutside = humidityOutside + (float) Math.random() * 5;
            }
            else {
                humidityOutside = humidityOutside - 2 + (float) Math.random() * 4;
            }
        }
        else {
            if (humidityOutside > (month.avgHumidity + 7)) {
                humidityOutside = humidityOutside - (float) Math.random() * 2;
            }
            else if (humidityOutside < (month.avgHumidity - 7)) {
                humidityOutside = humidityOutside + (float) Math.random() * 2;
            }
            else {
                humidityOutside = humidityOutside - (float) 0.5 + (float) Math.random();
            }
        }
        return  humidityOutside;
    }

    private Pair<Boolean, Float> generateHumidityInside(float humidityInside, boolean ventilate, boolean rain, float tempInside) {
        if (humidityInside > 82) {
            ventilate = true;
        }
        else if (humidityInside < 56 && ventilate) {
            ventilate = false;
        }

        if(ventilate) {
            if(rain) {
                humidityInside = humidityInside - (float) Math.random() * 2;
            }
            else {
                humidityInside = humidityInside - (float) Math.random() * 4;
            }
        }
        else {
            if(tempInside > 23) {
                humidityInside = humidityInside + (float) Math.random() * 2;
            }
            else {
                humidityInside = humidityInside + (float) Math.random();
            }
        }
        return new Pair<>(ventilate,humidityInside);
    }

    private float generateTempInside(float tempInside, float tempDifference, float brightness, boolean rain, boolean ventilate) {
        if(tempDifference > 10){
            if (brightness > 80) {
                tempInside = tempInside + (float) Math.random();
            }
            else {
                tempInside = tempInside + (float) Math.random() / 2;
            }
        }
        else if (tempDifference > 5) {
            if (brightness > 80) {
                tempInside = tempInside + (float) Math.random() / 2;
            }
            else {
                tempInside = tempInside + (float) Math.random() / 3;
            }
        }
        else if (tempDifference < -10) {
            if (brightness > 80) {
                tempInside = tempInside - (float) Math.random() / 3;
            }
            else {
                tempInside = tempInside - (float) Math.random() / 2;
            }
        }
        else if (tempDifference < -5) {
            if (brightness > 80) {
                tempInside = tempInside - (float) Math.random() / 4;
            }
            else {
                tempInside = tempInside - (float) Math.random() / 3;
            }
        }
        if(ventilate) {
            float factor = (float) 0.6;
            if(rain) {
                factor = (float) 0.3;
            }
            if(tempDifference - factor > 0) {
                tempInside = tempInside + (float) Math.random() * factor;
            }
            else if(tempDifference + factor < 0) {
                tempInside = tempInside - (float) Math.random() * factor;
            }
        }
        return tempInside;
    }

    private float generateTempOutside(float tempOutside, GeneratorMonth month, boolean rain, LocalTime currentTime, boolean fog, float brightness) {
        if (currentTime.isBefore(month.sunrise)) {
            tempOutside = tempOutside - (float) 0.1 + (float) Math.random() / 5;
        }
        else if (currentTime.isAfter(month.sunset)) {
            tempOutside = tempOutside - (float) Math.random() / 2;
        }
        else {
            if (rain) {
                float tempDiff = month.avgMaxTemp - tempOutside;
                if (tempDiff > 8) {
                    tempOutside = tempOutside + (float) Math.random() / 10;
                }
                else if (tempDiff < -2) {
                    tempOutside = tempOutside - (float) Math.random() / 2;
                }
                else {
                    tempOutside = tempOutside - (float) 0.2 + (float) Math.random() * (float) 0.4;
                }
            }
            else if (fog) {
                tempOutside = tempOutside + (float) Math.random() / 7;
            }
            else if (brightness > 75){
                tempOutside = tempOutside + (float) Math.random() / 3;
            }
            else if (brightness > 50) {
                tempOutside = tempOutside + (float) Math.random() / 8;
            }
            else if (brightness < 30){
                tempOutside = tempOutside - (float) 0.05 + (float) Math.random() / 10;
            }
            else {
                tempOutside = tempOutside - (float) 0.1 + (float) Math.random() / 5;
            }
        }

        if (tempOutside > month.absoluteMaxTemp) {
            tempOutside = month.absoluteMaxTemp;
        }
        else if (tempOutside < month.absoluteMinTemp) {
            tempOutside = month.absoluteMinTemp;
        }
        return tempOutside;
    }

    private Pair<Float, Integer> generateBrightness(float brightness, LocalTime currentTime, GeneratorMonth month, int entry, int noon, boolean rain, boolean fog, int leftNormalEntries, int brightEntries) {
        boolean dawn = false;
        if(currentTime.minusMinutes(80).isBefore(month.sunrise) || currentTime.plusMinutes(80).isAfter(month.sunset)) {
            dawn = true;
        }

        if(currentTime.isBefore(month.sunrise) || currentTime.isAfter(month.sunset)) {
            brightness = 0;
        }
        else if(rain) {
            if(dawn && brightness < 20 && entry < noon) {
                brightness = brightness + (float) Math.random() * 3;
            }
            else {
                brightness = 20 + random.nextInt(15) + (float) Math.random();
            }
        }
        else if(dawn && entry < noon && brightness < 60) {
            brightness = brightness + (float) Math.random() * 8;
        }
        else if (dawn && entry > noon) {
            brightness = brightness - (float) Math.random() * 8;
            if(brightness < 0) {
                brightness = 0;
            }
        }
        else if((leftNormalEntries == brightEntries || (!rain && !fog && brightEntries > 0 && generateWeightedDecision(0.65)))) {
            brightness = 85 + random.nextInt(15) + (float) Math.random();
            brightEntries--;
        }
        else {
            brightness = brightness - 5 + random.nextInt(10) + (float) Math.random();
            if (brightness > 80) {
                brightness = 80 + (float) Math.random();
            }
            else if (brightness < 40) {
                brightness = 40 + (float) Math.random();
            }
        }
        return new Pair<>(brightness, brightEntries);
    }
}
