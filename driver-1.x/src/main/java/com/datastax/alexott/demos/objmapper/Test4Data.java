package com.datastax.alexott.demos;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

// create KEYSPACE if not exists test WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 3};
// create table if not exists test.t4(id int, c int, t text, primary key(id, c));
@Table(name = "t4", keyspace = "test")
public class Test4Data {
    @PartitionKey
    int id;

    @ClusteringColumn
    @Column(name = "c")
    int clCol;

    @Column(name = "t")
    String text;

    public int getId() {
        return id;
    }

    public Test4Data() {

    }

    public Test4Data(int id, int clCol, String text) {
        this.id = id;
        this.clCol = clCol;
        this.text = text;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClCol() {
        return clCol;
    }

    public void setClCol(int clCol) {
        this.clCol = clCol;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Test4Data{" +
                "id=" + id +
                ", clCol=" + clCol +
                ", text='" + text + '\'' +
                '}';
    }
}
