package com.company.GreenhouseDataModels;

public class AlternativeOneGreenhouseData extends GreenhouseData{

    private int moistureSensValue2;
    private float tempSensValue2;
    private float humiditySensValue2;

    public AlternativeOneGreenhouseData(int id, int moistureSensValue1, int moistureSensValue2, float tempSensValue1,
                                        float tempSensValue2, float humiditySensValue1, float humiditySensValue2,
                                        float brightnessSensValue) {
        this.id = id;
        this.moistureSensValue1 = moistureSensValue1;
        this.moistureSensValue2 = moistureSensValue2;
        this.tempSensValue1 = tempSensValue1;
        this.tempSensValue2 = tempSensValue2;
        this.humiditySensValue1 = humiditySensValue1;
        this.humiditySensValue2 = humiditySensValue2;
        this.brightnessSensValue = brightnessSensValue;
    }

    //region Getter
    public int getMoistureSensValue2() {
        return moistureSensValue2;
    }

    public float getTempSensValue2() {
        return tempSensValue2;
    }

    public float getHumiditySensValue2() {
        return humiditySensValue2;
    }
    //endregion

    //region Setter


    public void setMoistureSensValue2(int moistureSensValue2) {
        this.moistureSensValue2 = moistureSensValue2;
    }

    public void setTempSensValue2(float tempSensValue2) {
        this.tempSensValue2 = tempSensValue2;
    }

    public void setHumiditySensValue2(float humiditySensValue2) {
        this.humiditySensValue2 = humiditySensValue2;
    }
    //endregion
}
