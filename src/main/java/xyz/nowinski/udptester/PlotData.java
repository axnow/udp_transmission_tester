package xyz.nowinski.udptester;

import lombok.Value;

import java.time.ZonedDateTime;
import java.util.List;

@Value
public class PlotData<T> {
    List<Long> points;
    List<T> values;

    public Long getMinX() {
        return points.get(0);
    }

    public Long getMaxX() {
        return points.get(points.size()-1);
    }
}
