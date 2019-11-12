package com.datastax.alexott.demos;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(name = "udt_test_fr", keyspace = "test")
public class UDTTestTableFR {
    @PartitionKey
    int id;
    @ClusteringColumn
    int cid;
    UDTTestType udt;

    public UDTTestTableFR(int id, int cid, UDTTestType udt) {
        this.id = id;
        this.cid = cid;
        this.udt = udt;
    }

    public UDTTestTableFR() {
    }

    @Override
    public String toString() {
        return "UDTTestTableFR{" +
                "id=" + id +
                ", cid=" + cid +
                ", udt=" + udt +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public UDTTestType getUdt() {
        return udt;
    }

    public void setUdt(UDTTestType udt) {
        this.udt = udt;
    }
}
