package com.datastax.alexott.demos.objmapper;

import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;

@Table(name = "scala_test_complex", keyspace = "test")
public class TableObjectClustered {
    int p1 = 0;
    int p2 = 0;
    int c1 = 0;
    int c2 = 0;
    String t = "";
    Date tm = new Date();

    TableObjectClustered() {
    }

    public int getP1() {
        return p1;
    }

    public void setP1(int p1) {
        this.p1 = p1;
    }

    public int getP2() {
        return p2;
    }

    public void setP2(int p2) {
        this.p2 = p2;
    }

    public int getC1() {
        return c1;
    }

    public void setC1(int c1) {
        this.c1 = c1;
    }

    public int getC2() {
        return c2;
    }

    public void setC2(int c2) {
        this.c2 = c2;
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
        return "TableObjectClustered{" +
                "p1=" + p1 +
                ", p2=" + p2 +
                ", c1=" + c1 +
                ", c2=" + c2 +
                ", t='" + t + '\'' +
                ", tm=" + tm +
                '}';
    }
}
