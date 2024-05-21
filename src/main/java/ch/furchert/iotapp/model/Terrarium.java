package ch.furchert.iotapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import static ch.furchert.iotapp.model.EState.*;

@Entity
public class Terrarium {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;
    private double temperature;
    private double humidity;
    private EState light;
    private EState nightLight;
    private EState rain;
    private EState water;
    private EState mqttState;

    // Konstruktor
    public Terrarium(String newName) {
        this.name = newName;
        temperature = -1;
        humidity = -1;
        light = UNDEFINED;
        nightLight = UNDEFINED;
        rain = UNDEFINED;
        water = UNDEFINED;
        mqttState = UNDEFINED;
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
        return light;
    }

    public void setLight(boolean lightOn) {
        if (lightOn) this.light = ON;
        else this.light = OFF;
    }

    public EState isNightLightOn() {
        return nightLight;
    }

    public void setNightLight(boolean nightLightOn) {
        if (nightLightOn) this.nightLight = ON;
        else this.nightLight = OFF;
    }

    public EState isRainOn() {
        return rain;
    }

    public void setRain(boolean rainOn) {
        if (rainOn) this.rain = ON;
        else this.rain = OFF;
    }

    public EState isWaterOn() {
        return water;
    }

    public void setWater(boolean waterOn) {
        if (waterOn) this.water = ON;
        else this.water = OFF;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EState getMqttState() {
        return mqttState;
    }

    public void setMqttState(boolean connected) {
        if (connected) this.mqttState = ON;
        else this.mqttState = OFF;
    }

    public EState getLight() {
        return light;
    }

    public EState getNightLight() {
        return nightLight;
    }

    public EState getRain() {
        return rain;
    }

    public EState getWater() {
        return water;
    }
}