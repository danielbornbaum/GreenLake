package GreenhouseDataModels;

public class AlternativeOneGreenhouseData extends GreenhouseData{

    private int moistureSensValue2;
    private float tempSensValue2;
    private float humiditySensValue2;

    public AlternativeOneGreenhouseData(int id) {
        this.id = id;
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

    public void setNewMeasurement(int moisture1, int moisture2, float temp1, float temp2, float humidity1,
                                  float humidity2, float brightness) {
        moistureSensValue1 = moisture1;
        moistureSensValue2 = moisture2;
        tempSensValue1 = temp1;
        tempSensValue2 = temp2;
        humiditySensValue1 = humidity1;
        humiditySensValue2 = humidity2;
        brightnessSensValue = brightness;
    }
}
