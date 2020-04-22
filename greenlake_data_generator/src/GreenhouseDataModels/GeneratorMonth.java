package GreenhouseDataModels;

public class GeneratorMonth {
    public Season season;
    public float avgMinTemp;
    public float absoluteMinTemp;
    public float avgMaxTemp;
    public float absoluteMaxTemp;
    public float daylight;
    public float sunshineHours;
    public float rainDays;
    public float avgHumidity;

    public GeneratorMonth (Season season, float avgMinTemp, float absoluteMinTemp, float avgMaxTemp, float absoluteMaxTemp,
                  float daylight, float sunshineHours, float rainDays, float avgHumidity) {
        this.season = season;
        this.avgMinTemp = avgMinTemp;
        this.absoluteMinTemp = absoluteMinTemp;
        this.avgMaxTemp = avgMaxTemp;
        this.absoluteMaxTemp = absoluteMaxTemp;
        this.daylight = daylight;
        this.sunshineHours = sunshineHours;
        this.rainDays = rainDays;
        this.avgHumidity = avgHumidity;
    }
}
