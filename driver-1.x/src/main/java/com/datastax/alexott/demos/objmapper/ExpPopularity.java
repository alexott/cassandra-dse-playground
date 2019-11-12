package com.datastax.alexott.demos;

import com.datastax.driver.mapping.annotations.UDT;

import java.util.Objects;

@UDT(keyspace = "src", name = "popularity")
public class ExpPopularity {

    String locale;
    double pop_a;
    double pop_b;

    public ExpPopularity(String locale, double pop_a, double pop_b) {
        this.locale = locale;
        this.pop_a = pop_a;
        this.pop_b = pop_b;
    }
    public ExpPopularity() {
        locale = "";
        pop_a = 0;
        pop_b = 0;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public double getPop_a() {
        return pop_a;
    }

    public void setPop_a(double pop_a) {
        this.pop_a = pop_a;
    }

    public double getPop_b() {
        return pop_b;
    }

    public void setPop_b(double pop_b) {
        this.pop_b = pop_b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpPopularity)) return false;
        ExpPopularity that = (ExpPopularity) o;
        return Double.compare(that.pop_a, pop_a) == 0 &&
                Double.compare(that.pop_b, pop_b) == 0 &&
                Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locale, pop_a, pop_b);
    }
}
