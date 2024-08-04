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
import java.time.temporal.ChronoUnit;
import java.util.List;
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

    public int[] queryStatus(String device){

        int[] historicState = new int[]{ -1,0,1,-1,0,1,-1,0,1,-1,0,1,-1,0,1,-1,0,1,-1,0,1,-1,0,1,-1 };

        String history = """
                from(bucket: "Terrarium")
                  |> range(start: -23h, stop: now())
                  |> filter(fn: (r) => r["device"] == "terra1")
                  |> filter(fn: (r) => r["_field"] == "MqttState")
                  |> aggregateWindow(every: 1h, fn: min, createEmpty: true)
                """;

        String last = """
                from(bucket: "Terrarium")
                  |> range(start: -30d, stop: now())
                  |> filter(fn: (r) => r["device"] == "terra1")
                  |> filter(fn: (r) => r["_field"] == "MqttState")
                  |> last()
                """;

        QueryApi queryApi = influxDBClient.getQueryApi();

        List<FluxTable> tables = queryApi.query(history);

        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            int i = 0;
            for (FluxRecord fluxRecord : records) {
                log.debug(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
                if (fluxRecord.getValueByKey("_value") != null) {
                    log.debug(fluxRecord.getValueByKey("_value").getClass().getSimpleName());
                    historicState[i++] = (int) fluxRecord.getValueByKey("_value");
                } else {
                    if(i==0){
                        historicState[i++] = -1;
                    } else {
                        historicState[i++] = historicState[i - 2];
                    }
                }
            }
        }

        Object lastValue = queryApi.query(last).getFirst().getRecords().getFirst().getValueByKey("_value");
        if(lastValue instanceof Integer){
            historicState[24] = (int) lastValue;
        } else {
            historicState[24] = -1;
        }

        return historicState;
    }

}
