package GreenhouseDataModels;

import java.util.Date;
import java.util.List;

public class AlternativeOneGreenhouseData implements IGreenhouseData {

    private int id;
    private int moistureSensValue1;
    private int moistureSensValue2;
    private float tempSensValue1;
    private float tempSensValue2;
    private float humiditySensValue1;
    private float humiditySensValue2;
    private float brightnessSensValue;

    public AlternativeOneGreenhouseData(int id) {
        this.id = id;
    }

    //region Getter
    public int getId() { return id; }

    public int getMoistureSensValue1() {
        return moistureSensValue1;
    }

    public int getMoistureSensValue2() {
        return moistureSensValue2;
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

    public void setMoistureSensValue1(int moistureSensValue1) { this.moistureSensValue1 = moistureSensValue1; }

    public void setMoistureSensValue2(int moistureSensValue2) {
        this.moistureSensValue2 = moistureSensValue2;
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

    }

}
