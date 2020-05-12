package GreenhouseDataModels;

import java.time.LocalTime;

public class GeneratorMonth {
    public Season season;
    public float avgMinTemp;
    public float absoluteMinTemp;
    public float avgMaxTemp;
    public float absoluteMaxTemp;
    public LocalTime sunrise;
    public LocalTime sunset;
    public float intenseSunHours;
    public int rainDays;
    public float avgHumidity;

    public GeneratorMonth (Season season, float avgMinTemp, float absoluteMinTemp, float avgMaxTemp, float absoluteMaxTemp, LocalTime sunrise, LocalTime sunset, float intenseSunHours, int rainDays, float avgHumidity) {
        this.season = season;
        this.avgMinTemp = avgMinTemp;
        this.absoluteMinTemp = absoluteMinTemp;
        this.avgMaxTemp = avgMaxTemp;
        this.absoluteMaxTemp = absoluteMaxTemp;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.intenseSunHours = intenseSunHours;
        this.rainDays = rainDays;
        this.avgHumidity = avgHumidity;
    }
}
