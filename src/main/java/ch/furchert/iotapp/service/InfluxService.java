package ch.furchert.iotapp.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.restriction.Restrictions;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import ch.furchert.iotapp.model.InfluxTerraData;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InfluxService {

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
    public void close() {
        if (this.influxDBClient != null) {
            this.influxDBClient.close();
        }
    }
}
