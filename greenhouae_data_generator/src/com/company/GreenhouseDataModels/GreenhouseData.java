package com.company.GreenhouseDataModels;

public abstract class GreenhouseData {
    protected int id;
    protected int moistureSensValue1;
    protected float tempSensValue1;
    protected float humiditySensValue1;
    protected float brightnessSensValue;

    public int getId() {
        return id;
    }

    public int getMoistureSensValue1() {
        return moistureSensValue1;
    }

    public float getTempSensValue1() {
        return tempSensValue1;
    }

    public float getHumiditySensValue1() {
        return humiditySensValue1;
    }

    public float getBrightnessSensValue() {
        return brightnessSensValue;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMoistureSensValue1(int moistureSensValue1) {
        this.moistureSensValue1 = moistureSensValue1;
    }

    public void setTempSensValue1(float tempSensValue1) {
        this.tempSensValue1 = tempSensValue1;
    }

    public void setHumiditySensValue1(float humiditySensValue1) {
        this.humiditySensValue1 = humiditySensValue1;
    }

    public void setBrightnessSensValue(float brightnessSensValue) {
        this.brightnessSensValue = brightnessSensValue;
    }
}
