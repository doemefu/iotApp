package ch.furchert.iotapp.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
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
        System.out.println("Querying data from InfluxDB");
        String flux = """
                from(bucket: "Terrarium")
                  |> range(start: -24h, stop: now())
                  |> aggregateWindow(every: 30m, fn: mean, createEmpty: false)
                  |> yield(name: "mean")""";

        //                  |> filter(fn: (r) => r["device"] == "terra1")
        List<FluxRecord> records = new ArrayList<>();

        AtomicBoolean done = new AtomicBoolean(false);

        queryApi.query(flux, (cancellable, fluxRecord) -> {
            records.add(fluxRecord);
            //System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
        }, throwable -> {
            System.out.println("Error occurred: " + throwable.getMessage());
            done.set(true);
        }, () -> {
            System.out.println("Query completed");
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