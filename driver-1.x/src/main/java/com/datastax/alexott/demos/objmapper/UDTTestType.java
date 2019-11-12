package com.datastax.alexott.demos;

import com.datastax.driver.mapping.annotations.UDT;

// CREATE TYPE test.tudt (id int, t text);
@UDT(name = "tudt", keyspace = "test")
public class UDTTestType {
    int id;
    String t;

    public UDTTestType(int id, String t) {
        this.id = id;
        this.t = t;
    }

    public UDTTestType() {
    }

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
}
