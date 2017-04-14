package io.github.wongben.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by WongBen on 2017-04-11.
 */
public class City {
    String id;
    String name;
    Map<String, List<Location>> locationMap;

    public City(String id, String name){
        this.id = id;
        this.name = name;
        this.locationMap = new HashMap<>();
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, List<Location>> getLocationMap() {
        return locationMap;
    }

    public void setLocationMap(Map<String, List<Location>> locationMap) {
        this.locationMap = locationMap;
    }
}
