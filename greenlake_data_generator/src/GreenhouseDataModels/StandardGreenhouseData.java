package GreenhouseDataModels;

public class StandardGreenhouseData extends GreenhouseData{

    private int moistureSensValue2;
    private int moistureSensValue3;
    private int moistureSensValue4;
    private float tempSensValue2;
    private float humiditySensValue2;

    public StandardGreenhouseData(int id) {
        this.id = id;
    }

    //region Getter
    public int getMoistureSensValue2() {
        return moistureSensValue2;
    }

    public int getMoistureSensValue3() {
        return moistureSensValue3;
    }

    public int getMoistureSensValue4() {
        return moistureSensValue4;
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

    public void setMoistureSensValue3(int moistureSensValue3) {
        this.moistureSensValue3 = moistureSensValue3;
    }

    public void setMoistureSensValue4(int moistureSensValue4) {
        this.moistureSensValue4 = moistureSensValue4;
    }

    public void setTempSensValue2(float tempSensValue2) {
        this.tempSensValue2 = tempSensValue2;
    }

    public void setHumiditySensValue2(float humiditySensValue2) {
        this.humiditySensValue2 = humiditySensValue2;
    }
    //endregion

    public void setNewMeasurement(int moisture1, int moisture2, int moisture3, int moisture4, float temp1, float temp2,
                                  float humidity1, float humidity2, float brightness) {
        moistureSensValue1 = moisture1;
        moistureSensValue2 = moisture2;
        moistureSensValue3 = moisture3;
        moistureSensValue4 = moisture4;
        tempSensValue1 = temp1;
        tempSensValue2 = temp2;
        humiditySensValue1 = humidity1;
        humiditySensValue2 = humidity2;
        brightnessSensValue = brightness;
    }

}
