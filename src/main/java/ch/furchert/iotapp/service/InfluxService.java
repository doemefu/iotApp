package ch.furchert.iotapp.service;

import ch.furchert.iotapp.controller.DataController;
import ch.furchert.iotapp.model.InfluxTerraData;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.restriction.Restrictions;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class InfluxService {

    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    @Value("${furchert.iotapp.influxToken}")
    private String token;

    @Value("${furchert.iotapp.influxOrg}")
    private String org;

    @Value("${furchert.iotapp.influxBucket}")
    private String bucket;

    @Value("${furchert.iotapp.influxHost}")
    private String InfluxURL;

    private InfluxDBClient influxDBClient;

    @PostConstruct
    public void init() {
        this.influxDBClient = InfluxDBClientFactory.create(InfluxURL, token.toCharArray(), org, bucket);
    }

    @PreDestroy
    public void close() {
        if (this.influxDBClient != null) {
            this.influxDBClient.close();
        }
    }

    public List<InfluxTerraData> queryMeasurementData() {
        // Erstelle die Flux-Abfrage mit dem flux-dsl Query Builder
        Flux fluxQuery = Flux
                .from("Terrarium")
                .range(-24L, ChronoUnit.HOURS)
                .filter(Restrictions.or(
                        Restrictions.field().equal("Humidity"),
                        Restrictions.field().equal("Temperature")
                ))
                .aggregateWindow(15L, ChronoUnit.MINUTES, "mean")
                .keep(new String[]{"_field", "_time", "application", "device", "_value"})
                .yield("median");

        QueryApi queryApi = this.influxDBClient.getQueryApi();

        // Führe die Abfrage aus und mappe die Ergebnisse auf MeasurementData-Objekte
        return queryApi.query(fluxQuery.toString(), InfluxTerraData.class);
    }

    public List<InfluxTerraData> queryMeasurementData(Instant start, Instant end) {
        // Erstelle die Flux-Abfrage mit dem flux-dsl Query Builder
        Flux fluxQuery = Flux
                .from("Terrarium")
                .range(start, end)
                .filter(Restrictions.or(
                        Restrictions.field().equal("Humidity"),
                        Restrictions.field().equal("Temperature")
                ))
                .aggregateWindow(15L, ChronoUnit.MINUTES, "mean")
                .keep(new String[]{"_field", "_time", "application", "device", "_value"})
                .yield("median");

        QueryApi queryApi = this.influxDBClient.getQueryApi();

        // Führe die Abfrage aus und mappe die Ergebnisse auf MeasurementData-Objekte
        return queryApi.query(fluxQuery.toString(), InfluxTerraData.class);
    }

    public double[] queryStatus(String device) {

        log.debug("Querying status for device: {}", device);

        double[] historicState = new double[]{ -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1 };

        String startHistory = String.format("""
            from(bucket: "Terrarium")
              |> range(start: -30d, stop: -23h)
              |> filter(fn: (r) => r["device"] == "%s")
              |> filter(fn: (r) => r["_field"] == "MqttState")
              |> last()
            """, device);

        String history = String.format("""
            from(bucket: "Terrarium")
              |> range(start: -23h, stop: now())
              |> filter(fn: (r) => r["device"] == "%s")
              |> filter(fn: (r) => r["_field"] == "MqttState")
            """, device);


        String last =  String.format("""
            from(bucket: "Terrarium")
              |> range(start: -7d, stop: now())
              |> filter(fn: (r) => r["device"] == "%s")
              |> filter(fn: (r) => r["_field"] == "MqttState")
              |> last()
            """, device);

        QueryApi queryApi = influxDBClient.getQueryApi();
        log.debug("queryApi buildup done");

        double lastKnownState = -1;

        // First value (long time period)
        List<FluxTable> startHistoryResult = queryApi.query(startHistory);
        if (!startHistoryResult.isEmpty() && !startHistoryResult.getFirst().getRecords().isEmpty()) {
            Object historyStart = startHistoryResult.getFirst().getRecords().getFirst().getValueByKey("_value");
            if (historyStart instanceof Double) {
                lastKnownState = (double) historyStart;
                historicState[0] = lastKnownState;
                log.debug("First value: {}", historyStart);
            }
        }

        // History values
        List<FluxTable> tables = queryApi.query(history);

        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            int oldHour = -1;
            int hourCounter = 1;
            for (FluxRecord fluxRecord : records) {
                Instant timestamp = fluxRecord.getTime();
                ZonedDateTime zonedDateTime = timestamp.atZone(ZoneId.of("UTC")); // Adjust ZoneId as needed
                int hour = zonedDateTime.getHour();

                Double value = (Double) fluxRecord.getValueByKey("_value");

                if (value != null) {
                    lastKnownState = value;  // Update last known state

                    if(historicState[hour] == -1) {
                        historicState[hour] = value;
                    }else{
                        historicState[hour] = historicState[hour] + value;
                    }
                }else {
                    historicState[hour] = lastKnownState;
                }

                if(oldHour == hour){
                    hourCounter++;
                }else{
                    historicState[hour] = historicState[hour] / hourCounter;
                    oldHour = hour;
                    hourCounter = 1;
                }
            }
        }

        // Fill in missing hours with the last known state up to that hour
        for (int i = 1; i < 24; i++) {
            if (historicState[i] == -1) {  // No data for this hour
                historicState[i] = historicState[i - 1];  // Use the state from the previous hour
            }
        }

        // Live value
        List<FluxTable> lastResult = queryApi.query(last);
        if (!lastResult.isEmpty() && !lastResult.getFirst().getRecords().isEmpty()) {
            Object lastValue = lastResult.getFirst().getRecords().getFirst().getValueByKey("_value");
            if (lastValue instanceof Double) {
                lastKnownState = (double) lastValue;
                historicState[24] = lastKnownState;
                log.debug("Last value: {}", lastValue);
            }
        }

        log.debug("Historic state: {}", historicState);

        return historicState;
    }


    private static double getLastKnownState(double[] historicState, int currentHour) {
        for (int i = currentHour - 1; i >= 0; i--) {
            if (historicState[i] != 0) {
                return historicState[i];
            }
        }
        return -1.0; // Default to -1.0 if no previous state is known
    }

}
