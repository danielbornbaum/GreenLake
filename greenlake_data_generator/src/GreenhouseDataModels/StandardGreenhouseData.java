package GreenhouseDataModels;

import javafx.util.Pair;

import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

public class StandardGreenhouseData implements IGreenhouseData {

    private int id;
    private LocalTime time;
    private int moistureSensValue1;
    private int moistureSensValue2;
    private int moistureSensValue3;
    private int moistureSensValue4;
    private float tempSensValue1;
    private float tempSensValue2;
    private float humiditySensValue1;
    private float humiditySensValue2;
    private float brightnessSensValue;

    public StandardGreenhouseData(int id) {
        this.id = id;
    }

    //region Getter
    public int getId() { return id; }

    public LocalTime getTime() { return time; }

    public int getMoistureSensValue1() {
        return moistureSensValue1;
    }

    public int getMoistureSensValue2() {
        return moistureSensValue2;
    }

    public int getMoistureSensValue3() {
        return moistureSensValue3;
    }

    public int getMoistureSensValue4() {
        return moistureSensValue4;
    }

    public float getTempSensValue1() {
        return tempSensValue1;
    }

    public float getTempSensValue2() {
        return tempSensValue2;
    }

    public float getHumiditySensValue1() {
        return humiditySensValue1;
    }

    public float getHumiditySensValue2() {
        return humiditySensValue2;
    }

    public float getBrightnessSensValue() {
        return brightnessSensValue;
    }
    //endregion

    //region Setter
    public void setId(int id) { this.id = id; }

    public void setTime(LocalTime time) { this.time = time; }

    public void setMoistureSensValue1(int moistureSensValue1) { this.moistureSensValue1 = moistureSensValue1; }

    public void setMoistureSensValue2(int moistureSensValue2) {
        this.moistureSensValue2 = moistureSensValue2;
    }

    public void setMoistureSensValue3(int moistureSensValue3) {
        this.moistureSensValue3 = moistureSensValue3;
    }

    public void setMoistureSensValue4(int moistureSensValue4) {
        this.moistureSensValue4 = moistureSensValue4;
    }

    public void setTempSensValue1(float tempSensValue1) { this.tempSensValue1 = tempSensValue1; }

    public void setTempSensValue2(float tempSensValue2) {
        this.tempSensValue2 = tempSensValue2;
    }

    public void setHumiditySensValue1(float humiditySensValue1) { this.humiditySensValue1 = humiditySensValue1; }

    public void setHumiditySensValue2(float humiditySensValue2) {
        this.humiditySensValue2 = humiditySensValue2;
    }

    public void setBrightnessSensValue(float brightnessSensValue) { this.brightnessSensValue = brightnessSensValue; }
    //endregion

    public Pair<List<IGreenhouseData>, Integer> generateNewDay(int secondInterval, int monthRainDays, GeneratorMonth month, Date date, float lastTemperatureIn, float lastTemperatureOut, float lastHumidityIn, float lastHumidityOut) {
        Random random = new Random();
        List<IGreenhouseData> day = new ArrayList<IGreenhouseData>();
        int monthNr = date.getMonth();
        int yearNr = date.getYear();
        int monthDays = YearMonth.of(yearNr, monthNr).lengthOfMonth();
        int entryCount = (24 * 3600) / secondInterval;
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
        float humidEntryBeforeRain = 0;
        boolean fog = false;
        float tempInside = 0;
        float tempOutside = 0;
        float humidityInside = 0;
        float humidityOutside = 0;
        float brightness = 0;
        float tempDifference = 0;
        float rainStartTemp = 0;

        //Generate special conditions (rain / fog)
        if(generateWeightedDecision(0.4) || monthDays - date.getDay() == monthRainDays) {
            monthRainDays--;
            int rainEntries = (int) Math.round((1 + random.nextInt(9)) * 3600 / secondInterval);
            leftNormalEntries = leftNormalEntries - rainEntries;
            rainStartEntry = random.nextInt(entryCount - rainEntries);
            rainEndEntry = rainStartEntry + rainEntries - 1;
            if (month.season == Season.SUMMER && generateWeightedDecision(0.35) &&
                    rainStartEntry > (10 * 3600 / secondInterval)) {
                humidEntryBeforeRain = rainStartEntry - Math.round(random.nextInt(8) / 2 * 3600 / secondInterval);
            }
            else {
                humidBeforeRain = false;
            }
        }
        if (month.season == Season.FALL && generateWeightedDecision(0.84)) {
            int fogEntriesSinceSunrise = (int) Math.round((1 + random.nextInt(3)) * 3600 / secondInterval);
            leftNormalEntries = leftNormalEntries - fogEntriesSinceSunrise;
            fogEndEntry = (int) Math.round((month.sunrise.getMinute() * 60 + month.sunrise.getHour() * 3600 + 3600 * Math.random() * 0.25) / secondInterval);
        }

        //Generate first constant entry
        StandardGreenhouseData data = new StandardGreenhouseData(1);
        data.setTempSensValue1(lastTemperatureOut);
        data.setTempSensValue2(lastTemperatureIn);
        data.setHumiditySensValue1(lastHumidityOut);
        data.setHumiditySensValue2(lastHumidityIn);
        data.setTime(LocalTime.of(0,0));
        data.setBrightnessSensValue(0);
        data.setMoistureSensValue1(70);
        data.setMoistureSensValue2(70);
        data.setMoistureSensValue3(70);
        data.setMoistureSensValue4(70);

        day.add(data);
        StandardGreenhouseData lastInstance = data;

        //Loop generating all other entries
        for (int entry = 1; entry < entryCount; entry++) {
            brightness = lastInstance.getBrightnessSensValue();
            tempOutside = lastInstance.getTempSensValue1();
            tempInside = lastInstance.getTempSensValue2();
            tempDifference = tempOutside - tempInside;
            humidityOutside = lastInstance.getHumiditySensValue1();
            humidityInside = lastInstance.getHumiditySensValue2();
            if (humidEntryBeforeRain <= entry && entry < rainStartEntry) {
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

            //Set Time DONE
            data.setTime(lastInstance.getTime().plusSeconds(secondInterval));

            //Set Moisture DONE
            data.setMoistureSensValue1(generateMoisture(lastInstance.getMoistureSensValue1()));
            data.setMoistureSensValue2(generateMoisture(lastInstance.getMoistureSensValue2()));
            data.setMoistureSensValue3(generateMoisture(lastInstance.getMoistureSensValue3()));
            data.setMoistureSensValue4(generateMoisture(lastInstance.getMoistureSensValue4()));

            //Set Temperature DONE
            //Outside DONE
            if (data.getTime().isBefore(month.sunrise)) {
                tempOutside = tempOutside - (float) 0.1 + (float) Math.random() / 5;
            }
            else if (data.getTime().isAfter(month.sunset)) {
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
            data.setTempSensValue1(tempOutside);

            //Inside DONE
            if(tempDifference > 10){
                if (brightness > 80) {
                    tempInside = tempInside + (float) Math.random();
                }
                else {
                    tempInside = tempInside + (float) Math.random() / 2;
                }
            }
            else if (tempDifference > 5) {
                if (lastInstance.getBrightnessSensValue() > 80) {
                    tempInside = tempInside + (float) Math.random() / 2;
                }
                else {
                    tempInside = tempInside + (float) Math.random() / 3;
                }
            }
            else if (tempDifference < -5) {
                if (lastInstance.getBrightnessSensValue() > 80) {
                    tempInside = tempInside - (float) Math.random() / 4;
                }
                else {
                    tempInside = tempInside - (float) Math.random() / 3;
                }
            }
            else if (tempDifference < -10) {
                if (lastInstance.getBrightnessSensValue() > 80) {
                    tempInside = tempInside - (float) Math.random() / 3;
                }
                else {
                    tempInside = tempInside - (float) Math.random() / 2;
                }
            }
            data.setTempSensValue2(tempInside);

            //Set Brightness DONE
            if(data.getTime().isBefore(month.sunrise) || data.getTime().isAfter(month.sunset)) {
                brightness = 0;
            }
            else if(rain) {
                brightness = 20 + random.nextInt(15);
            }
            else if(leftNormalEntries == brightEntries || (!rain && !fog && brightEntries > 0 && generateWeightedDecision(0.65))) {
                brightness = 85 + random.nextInt(15);
                brightEntries--;
            }
            else {
                brightness = brightness - 5 + random.nextInt(10);
                if (brightness > 83) {
                    brightness = 82;
                }
                else if (brightness < 40) {
                    brightness = 41;
                }
            }
            data.setBrightnessSensValue(brightness);

            //Set Humidity DONE
            // Outside DONE
            if (fog) {
                humidityOutside = 95 + (float) Math.random() * 5;
            }
            else if (rain || (humidBeforeRain && entry >= humidEntryBeforeRain)) {
                if (humidityOutside < 70) {
                    humidityOutside = humidityOutside + (float) Math.random() * 10;
                } else {
                    humidityOutside = humidityOutside - 3 + (float) Math.random() * 6;
                    if (humidityOutside < 70) {
                        humidityOutside = 70;
                    } else if (humidityOutside > 90) {
                        humidityOutside = 90;
                    }
                }
            }
            else if (month.season == Season.SUMMER && noon < entry && entry < afternoon) {
                if (humidityOutside > 55) {
                    humidityOutside = humidityOutside - (float) Math.random() * 5;
                }
                else if (humidityOutside < 40) {
                    humidityOutside = 41;
                }
                else {
                    humidityOutside = humidityOutside - 2 + (float) Math.random() * 4;
                }
            }
            else if (month.season == Season.WINTER && brightness > 70) {
                if (humidityOutside > 85) {
                    humidityOutside = 84;
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
            data.setHumiditySensValue1(humidityOutside);

            //Inside DONE
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
                humidityInside = humidityInside + (float) Math.random();
            }
            data.setHumiditySensValue2(humidityInside);

            day.add(data);
            lastInstance = data;
        }

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

    private int generateMoisture(int lastMoisture){
        if(lastMoisture < 70) {
            return 82;
        }
        else if(generateWeightedDecision(0.5)){
            return lastMoisture - 2;
        }
        else {
            return lastMoisture - 1;
        }
    }

}
