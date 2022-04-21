package xyz.nowinski.udptester;

import lombok.Value;

import java.util.Comparator;
import java.util.List;

@Value
public class PlotData<T> {
    List<Long> points;
    List<T> values;

    public Long getMinX() {
        return points.get(0);
    }

    public Long getMaxX() {
        return points.get(points.size() - 1);
    }

    public double getMaxValue() {
        return values.stream().map(v -> v instanceof Number ? ((Number) v).doubleValue() : 0.0)
                .max(Comparator.comparing(v -> ((Number) v).doubleValue())).orElse(0.0);
    }
}
