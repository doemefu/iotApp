package ch.furchert.iotapp.controller;

import ch.furchert.iotapp.service.Influx;
import ch.furchert.iotapp.service.InfluxService;
import com.influxdb.query.FluxRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import ch.furchert.iotapp.model.InfluxTerraData;

@RestController
@RequestMapping("/api/data")
public class DataController {

    @Autowired
    private Influx influx;

    @GetMapping("/influxData")
    public ResponseEntity<List<FluxRecord>> getInfluxData() {
        System.out.println("Getting data from InfluxDB");
        List<FluxRecord> records = influx.query();
        //System.out.println("die records: " + records);
        return new ResponseEntity<>(records, HttpStatus.OK);
    }

    @Autowired
    private InfluxService influxService; // Anpassung auf den korrekten Service-Typ

    @GetMapping("/influxDataNew")
    public ResponseEntity<List<InfluxTerraData>> getInfluxDataNew() { // Anpassung des Rückgabetyps
        System.out.println("Getting data from InfluxDB");
        List<InfluxTerraData> data = influxService.queryMeasurementData(); // Verwende die neue Methode
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
