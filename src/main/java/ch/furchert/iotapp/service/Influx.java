package ch.furchert.iotapp.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class Influx {

    //private static final char[] token = "ovfI55M7pjuw15bmeqKhwbO1FdyXcgw_1oD1nEhNnvn7B_dibqK0TGunR-a4HMif_GCo6cbSh_8_vP0kX_kWXw==".toCharArray();
    private static final char[] token = "EgdYFJOptmMPvxKs-NH7aeAJJ7GznekrgARnYeM64tts2yeF2p396dT-BBJiEWVbKGBLF6D1hwPRYwKbDPBvmA==".toCharArray();
    private static final String org = "iotApp";
    private static final String bucket = "Terrarium";
    private static final String InfluxURL = "http://influxdb:8086";
    private InfluxDBClient influxDBClient;
    private QueryApi queryApi;

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