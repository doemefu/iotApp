package ch.furchert.iotapp.model;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;

import java.time.Instant;

@Measurement(name = "InfluxTerraData")
public class InfluxTerraData {

    @Column(tag = true)
    private String device;

    @Column(tag = true)
    private String application;

    @Column
    private String _field;

    @Column(timestamp = true)
    private Instant _time;

    @Column
    private Double _value;

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String get_field() {
        return _field;
    }

    public void set_field(String _field) {
        this._field = _field;
    }

    public Instant get_time() {
        return _time;
    }

    public void set_time(Instant _time) {
        this._time = _time;
    }

    public Double get_value() {
        return _value;
    }

    public void set_value(Double _value) {
        this._value = _value;
    }
}
