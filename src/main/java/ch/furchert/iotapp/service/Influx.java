package ch.furchert.iotapp.service;

import ch.furchert.iotapp.controller.DataController;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class Influx {

    @Value("${furchert.iotapp.influxToken}")
    private String token;

    @Value("${furchert.iotapp.influxOrg}")
    private String org;

    @Value("${furchert.iotapp.influxBucket}")
    private String bucket;

    @Value("${furchert.iotapp.influxHost}")
    private String InfluxURL;

    private static final Logger log = LoggerFactory.getLogger(Influx.class);

    private InfluxDBClient influxDBClient;
    private QueryApi queryApi;

    @PostConstruct
    public void init() {
        this.influxDBClient = InfluxDBClientFactory.create(InfluxURL, token.toCharArray(), org, bucket);
        this.queryApi = influxDBClient.getQueryApi();
    }

    @PreDestroy
    public void close() {
        if (influxDBClient != null) {
            influxDBClient.close();
        }
    }

    public List<FluxRecord> query() {
        log.trace("Querying data from InfluxDB");
        String flux = """
                from(bucket: "Terrarium")
                  |> range(start: -24h, stop: now())
                  |> aggregateWindow(every: 30m, fn: mean, createEmpty: false)
                  |> yield(name: "mean")""";

        List<FluxRecord> records = new ArrayList<>();

        AtomicBoolean done = new AtomicBoolean(false);

        queryApi.query(flux, (cancellable, fluxRecord) -> {
            records.add(fluxRecord);
            //System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
        }, throwable -> {
            log.error("Error occurred: {}", throwable.getMessage());
            done.set(true);
        }, () -> {
            log.info("Query completed");
            done.set(true);
        });

        while (!done.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return records;
    }

}