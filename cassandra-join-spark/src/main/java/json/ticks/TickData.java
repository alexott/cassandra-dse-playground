package json.ticks;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TickData {
    private static final LocalDateTime BASE_TIME = LocalDateTime.now();
    private static final AtomicLong TIME_OFFSET = new AtomicLong();

    private String symbol;
    private double value;
    private String datetime;

    public TickData(String symbol, double value) {
        this.symbol = symbol;
        this.value = value;
        this.datetime = BASE_TIME.plus(TIME_OFFSET.incrementAndGet(), ChronoUnit.SECONDS).toString();
    }

    public String getSymbol() {
        return symbol;
    }

    public double getValue() {
        return value;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(){
        this.datetime = BASE_TIME.plus(TIME_OFFSET.incrementAndGet(), ChronoUnit.SECONDS).toString();
    }

    public void setValue(double v){
        this.value = v;
    }

    @Override
    public String toString() {
        return "TickData [" +
                "symbol=" + symbol + ", " +
                "value=" + value + "]";
    }

}

