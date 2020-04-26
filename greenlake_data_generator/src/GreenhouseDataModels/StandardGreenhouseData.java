package GreenhouseDataModels;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public List<IGreenhouseData> generateNewDay(int secondInterval, int monthRainDays, GeneratorMonth month, Date date, float lastTemperatureIn, float lastTemperatureOut, float lastHumidityIn, float lastHumidityOut) {
        List<IGreenhouseData> day = new ArrayList<IGreenhouseData>();
        int entryCount = (24 * 3600) / secondInterval;
        boolean rain = false;
        double rainDuration;
        boolean fog = false;
        double fogDurationSinceSunrise;

        if (monthRainDays < month.rainDays) {
            rain = true;
            rainDuration = Math.random() * 10 * 3600;
        }
        if (month.season == Season.FALL) {
            fog = generateWeightedDecision(0.84);
            fogDurationSinceSunrise = Math.random() * 4 * 3600;
        }

        //TODO calc rain start/end

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

        for (int entry = 1; entry < entryCount; entry++) {
            StandardGreenhouseData lastInstance = (StandardGreenhouseData) day.get(entry - 1);

            //Set Time DONE
            data.setTime(lastInstance.getTime().plusSeconds(secondInterval));

            //Set Moisture DONE
            data.setMoistureSensValue1(generateMoisture(lastInstance.getMoistureSensValue1()));
            data.setMoistureSensValue2(generateMoisture(lastInstance.getMoistureSensValue2()));
            data.setMoistureSensValue3(generateMoisture(lastInstance.getMoistureSensValue3()));
            data.setMoistureSensValue4(generateMoisture(lastInstance.getMoistureSensValue4()));

            //Set Brightness
            if(data.getTime().isBefore(month.sunrise) || data.getTime().isBefore(month.sunset)) {
                data.setBrightnessSensValue(0);
            }
            //TODO bei Regen
            //TODO tagsüber

            //Set Temperature
            //TODO temp außen (Regen/Nebel/Sonne)
            //TODO temp innen (Sonne/Temp außen)

            //Set Humidity
            //TODO humidity außen (Regen/Nebel)
            //TODO humidity innen (bei Regen -> nur Vent = langsamere Abnahme)
        }

        return day;
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
