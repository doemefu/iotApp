package ch.furchert.iotapp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

@Service
public class Influx {

    private InfluxDBClient influxDBClient;
    private QueryApi queryApi;

    private static final char[] token = "ovfI55M7pjuw15bmeqKhwbO1FdyXcgw_1oD1nEhNnvn7B_dibqK0TGunR-a4HMif_GCo6cbSh_8_vP0kX_kWXw==".toCharArray();
    private static final String org = "m153";
    private static final String bucket = "doemesPlants";
    private static final String InfluxURL = "http://52.236.138.112:8086";


    @PostConstruct
    public void init() {
        this.influxDBClient = InfluxDBClientFactory.create(InfluxURL, token, org, bucket);
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
                from(bucket: "doemesPlants")
                  |> range(start: -24h, stop: now())
                  |> filter(fn: (r) => r["device"] == "terra1")
                  |> aggregateWindow(every: 30m, fn: mean, createEmpty: false)
                  |> yield(name: "mean")""";
        List<FluxRecord> records = new ArrayList<>();

        AtomicBoolean done = new AtomicBoolean(false);

        queryApi.query(flux, (cancellable, fluxRecord) -> {
            records.add(fluxRecord);
            System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
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