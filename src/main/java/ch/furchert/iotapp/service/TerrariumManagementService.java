package ch.furchert.iotapp.service;

import ch.furchert.iotapp.model.Terrarium;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TerrariumManagementService {

    private final Map<String, Terrarium> terrariums = new HashMap<>();

    @PostConstruct
    public void init() {
        // Initialisiere die Terrarium-Instanzen bei Anwendungsstart
        terrariums.put("terra1", new Terrarium("terra1"));
        terrariums.put("terra2", new Terrarium("terra2"));
    }

    public Terrarium getTerrarium(String id) {
        return terrariums.get(id);
    }

    // Weitere Methoden zur Verwaltung der Terrarien...
}
