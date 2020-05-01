package GreenhouseDataModels;

import javafx.util.Pair;

import java.util.Date;
import java.util.List;

public interface IGreenhouseData {

    int getId();

    int getMoistureSensValue1();

    float getTempSensValue1();

    float getTempSensValue2();

    float getHumiditySensValue1();

    float getHumiditySensValue2();

    float getBrightnessSensValue();

    void setId(int id);

    void setMoistureSensValue1(int moistureSensValue1);

    void setTempSensValue1(float tempSensValue1);

    void setTempSensValue2(float tempSensValue2);

    void setHumiditySensValue1(float humiditySensValue1);

    void setHumiditySensValue2(float humiditySensValue2);

    void setBrightnessSensValue(float brightnessSensValue);

    Pair<List<IGreenhouseData>, Integer> generateNewDay(int secondInterval, int monthRainDays, GeneratorMonth month, Date date, float lastTemperatureIn, float lastTemperatureOut, float lastHumidityIn, float lastHumidityOut);
}
