package com.datastax.alexott.demos;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.List;
import java.util.Map;
import java.util.Set;

// create table test.audit_test(id int primary key, u test.audit_type, s set<text>, l list<text>, m map<int, text>);
@Table(keyspace="test", name="audit_test")
public class AuditTestTable {

    @PartitionKey
    int id;
    AuditTestType u;
    Set<String> s;
    List<String> l;
    Map<Integer, String> m;

    public AuditTestTable() {
    }

    public AuditTestTable(int id, AuditTestType u, Set<String> s, List<String> l, Map<Integer, String> m) {
        this.id = id;
        this.u = u;
        this.s = s;
        this.l = l;
        this.m = m;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AuditTestType getU() {
        return u;
    }

    public void setU(AuditTestType u) {
        this.u = u;
    }

    public Set<String> getS() {
        return s;
    }

    public void setS(Set<String> s) {
        this.s = s;
    }

    public List<String> getL() {
        return l;
    }

    public void setL(List<String> l) {
        this.l = l;
    }

    public Map<Integer, String> getM() {
        return m;
    }

    public void setM(Map<Integer, String> m) {
        this.m = m;
    }

    @Override
    public String toString() {
        return "AuditTestTable{" +
                "id=" + id +
                ", u=" + u +
                ", s=" + s +
                ", l=" + l +
                ", m=" + m +
                '}';
    }
}
