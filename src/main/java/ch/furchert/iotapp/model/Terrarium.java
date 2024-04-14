package ch.furchert.iotapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import static ch.furchert.iotapp.model.EState.*;

@Entity // Markiert die Klasse als JPA-Entität
public class Terrarium {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;
    private double temperature;
    private double humidity;
    private EState lightOn;
    private EState nightLightOn;
    private EState rainOn;
    private EState waterOn;

    // Konstruktor
    public Terrarium(String newName) {
        this.name = newName;
        temperature = -1;
        humidity = -1;
        lightOn = UNDEFINED;
        nightLightOn = UNDEFINED;
        rainOn = UNDEFINED;
        waterOn = UNDEFINED;
    }

    public Terrarium() {

    }

    // Getter und Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public EState isLightOn() {
        return lightOn;
    }

    public void setLightOn(boolean lightOn) {
        if (lightOn) this.lightOn = ON;
        else this.lightOn = OFF;
    }

    public EState isNightLightOn() {
        return nightLightOn;
    }

    public void setNightLightOn(boolean nightLightOn) {
        if (nightLightOn) this.nightLightOn = ON;
        else this.nightLightOn = OFF;
    }

    public EState isRainOn() {
        return rainOn;
    }

    public void setRainOn(boolean rainOn) {
        if (rainOn) this.rainOn = ON;
        else this.rainOn = OFF;
    }

    public EState isWaterOn() {
        return waterOn;
    }

    public void setWaterOn(boolean waterOn) {
        if (waterOn) this.waterOn = ON;
        else this.waterOn = OFF;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}