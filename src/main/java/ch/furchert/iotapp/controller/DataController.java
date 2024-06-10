package ch.furchert.iotapp.controller;

import ch.furchert.iotapp.service.Influx;
import ch.furchert.iotapp.service.InfluxService;
import com.influxdb.query.FluxRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import ch.furchert.iotapp.model.InfluxTerraData;

@RestController
@RequestMapping("/api/data")
public class DataController {

    @Autowired
    private Influx influx;

    @Autowired
    private InfluxService influxService; // Anpassung auf den korrekten Service-Typ

    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    @GetMapping("/influxData")
    public ResponseEntity<List<FluxRecord>> getInfluxData() {
        log.trace("Getting data from InfluxDB");
        List<FluxRecord> records = influx.query();
        log.debug("die records: {}", records);
        return new ResponseEntity<>(records, HttpStatus.OK);
    }

    @GetMapping("/influxDataNew")
    public ResponseEntity<List<InfluxTerraData>> getInfluxDataNew(
            @RequestParam(value = "period", defaultValue = "24h") String period) {

        log.trace("Getting data from InfluxDB for period: {}", period);
        Instant endTime = Instant.now(); // Das aktuelle Datum und Uhrzeit
        Instant startTime = calculateStartTime(endTime, period);

        List<InfluxTerraData> data = influxService.queryMeasurementData(startTime, endTime);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    private Instant calculateStartTime(Instant endTime, String period) {
        ChronoUnit unit = getChronoUnit(period);
        int amount = Integer.parseInt(period.replaceAll("\\D", "")); // Extrahiert die Zahl aus dem String
        return endTime.minus(amount, unit);
    }

    private ChronoUnit getChronoUnit(String period) {
        if (period.endsWith("h")) {
            return ChronoUnit.HOURS;
        } else if (period.endsWith("d")) {
            return ChronoUnit.DAYS;
        } else if (period.endsWith("mon")) {
            return ChronoUnit.MONTHS;
        } else if (period.endsWith("y")) {
            return ChronoUnit.YEARS;
        } else if (period.endsWith("yrs")) {
            return ChronoUnit.YEARS;
        } else {
            throw new IllegalArgumentException("Invalid time period format");
        }
    }
}
