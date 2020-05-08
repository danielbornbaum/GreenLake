package GreenhouseDataModels;

import javafx.util.Pair;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public interface IGreenhouseData {

    int getId();

    Calendar getTime();

    int getMoistureSensValue1();

    float getTempSensValue1();

    float getTempSensValue2();

    float getHumiditySensValue1();

    float getHumiditySensValue2();

    float getBrightnessSensValue();

    void setId(int id);

    void setTime(Calendar time);

    void setMoistureSensValue1(int moistureSensValue1);

    void setTempSensValue1(float tempSensValue1);

    void setTempSensValue2(float tempSensValue2);

    void setHumiditySensValue1(float humiditySensValue1);

    void setHumiditySensValue2(float humiditySensValue2);

    void setBrightnessSensValue(float brightnessSensValue);

    Pair<List<IGreenhouseData>, Integer> generateNewDay(int secondInterval, int monthRainDays, GeneratorMonth month, Calendar calendar, IGreenhouseData lastEntry);
}
