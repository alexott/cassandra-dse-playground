package com.datastax.alexott.demos;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Set;

@Table(name = "entities_udt", keyspace = "srs")
public class ExpEntity {

    @PartitionKey
    int hcom_geo_id;

    Set<ExpPopularity> popularity;

    public int getHcom_geo_id() {
        return hcom_geo_id;
    }

    public void setHcom_geo_id(int hcom_geo_id) {
        this.hcom_geo_id = hcom_geo_id;
    }

    public Set<ExpPopularity> getPopularity() {
        return popularity;
    }

    public void setPopularity(Set<ExpPopularity> popularity) {
        this.popularity = popularity;
    }
}
