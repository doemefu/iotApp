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

    public double[] queryStatus(String device){

        log.debug("Querying status for device: {}", device);

        double [] historicState = new double[]{ -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1 };

        String startHistory = """
                from(bucket: "Terrarium")
                  |> range(start: -7d, stop: -23h)
                  |> filter(fn: (r) => r["device"] == "terra1")
                  |> filter(fn: (r) => r["_field"] == "MqttState")
                  |> last()
                """;

        String history = """
                from(bucket: "Terrarium")
                  |> range(start: -23h, stop: now())
                  |> filter(fn: (r) => r["device"] == "terra1")
                  |> filter(fn: (r) => r["_field"] == "MqttState")
                  |> aggregateWindow(every: 1h, fn: mean, createEmpty: true)
                """;

        String last = """
                from(bucket: "Terrarium")
                  |> range(start: -7d, stop: now())
                  |> filter(fn: (r) => r["device"] == "terra1")
                  |> filter(fn: (r) => r["_field"] == "MqttState")
                  |> last()
                """;

        QueryApi queryApi = influxDBClient.getQueryApi();
        log.debug("queryApi buildup done");

        //first value
        Object historyStart = queryApi.query(startHistory).getFirst().getRecords().getFirst().getValueByKey("_value");
        if(historyStart instanceof Double){
            historicState[0] = (double) historyStart;
            log.debug("First value: {}", historyStart);
        }

        //history values
        List<FluxTable> tables = queryApi.query(history);

        Map<Integer, Double> hourlyStates = new HashMap<>();
        Map<Integer, Integer> hourlyCounts = new HashMap<>();

        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();

            for (FluxRecord fluxRecord : records) {
                Instant timestamp = fluxRecord.getTime();
                ZonedDateTime zonedDateTime = timestamp.atZone(ZoneId.of("UTC")); // Adjust ZoneId as needed
                int hour = zonedDateTime.getHour();

                Double value = (Double) fluxRecord.getValueByKey("_value");

                if (value != null) {
                    hourlyStates.put(hour, hourlyStates.getOrDefault(hour, 0.0) + value);
                    hourlyCounts.put(hour, hourlyCounts.getOrDefault(hour, 0) + 1);
                }
            }
        }

        for (int i = 1; i < 24; i++) {
            if (hourlyStates.containsKey(i)) {
                historicState[i] = hourlyStates.get(i) / hourlyCounts.get(i);
            } else {
                historicState[i] = getLastKnownState(historicState, i);
            }
        }

        //live value
        Object lastValue = queryApi.query(last).getFirst().getRecords().getFirst().getValueByKey("_value");
        if(lastValue instanceof Double){
            historicState[24] = (double) lastValue;
            log.debug("Last value: {}", lastValue);
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
