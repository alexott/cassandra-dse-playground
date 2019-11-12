package com.datastax.alexott.demos.objmapper;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;

@Table(name="scala_test", keyspace = "test")
public class TableObjJava {
    @PartitionKey
    int id = 0;
    String t = "";
    Date tm = new Date();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public Date getTm() {
        return tm;
    }

    public void setTm(Date tm) {
        this.tm = tm;
    }

    @Override
    public String toString() {
        return "TableObjJava{" +
                "id=" + id +
                ", t='" + t + '\'' +
                ", tm=" + tm +
                '}';
    }
}
