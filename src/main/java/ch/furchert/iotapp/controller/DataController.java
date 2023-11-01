package ch.furchert.iotapp.controller;

import ch.furchert.iotapp.service.Influx;
import com.influxdb.query.FluxRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/data")
public class DataController {

    @Autowired
    private Influx influxService;

    @GetMapping("/influxData")
    public ResponseEntity<List<FluxRecord>> getInfluxData() {
        System.out.println("Getting data from InfluxDB");
        List<FluxRecord> records = influxService.query();
        System.out.println("die records: " + records);
        return new ResponseEntity<>(records, HttpStatus.OK);
    }
}
