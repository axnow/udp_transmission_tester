package xyz.nowinski.udptester;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PackageTrackerTest {

    PackageTracker instance;

    @BeforeEach
    public void prepareInstance() {
        this.instance=new PackageTracker();
    }

    @Test
    public void testPlot() {


        ZonedDateTime zt = ZonedDateTime.of(LocalDateTime.of(2020, 6, 1, 13, 30), ZoneId.of("UTC"));
        long windowStart = zt.toInstant().toEpochMilli();
        long windowEnd=windowStart+60*1000;

        //first test empty instance:
        PlotData<Integer> sentPackagePlot = instance.getSentPackagePlot(windowStart, windowEnd, 1);
        assertEquals(61, sentPackagePlot.getPoints().size());
        assertEquals(IntStream.range(0, 61).mapToObj(i->0).collect(Collectors.toList()),sentPackagePlot.getValues());

        PlotData<Integer> receivedPackagePlot = instance.getRespondedPackagePlots(windowStart, windowEnd, 1);
        assertEquals(61, receivedPackagePlot.getPoints().size());
        assertEquals(IntStream.range(0, 61).mapToObj(i->0).collect(Collectors.toList()),receivedPackagePlot.getValues());

        PlotData<Double> delayPlot = instance.getResponseDelayPlot(windowStart, windowEnd, 1);
        assertEquals(61, delayPlot.getPoints().size());
        assertEquals(IntStream.range(0, 61).mapToObj(i->null).collect(Collectors.toList()),delayPlot.getValues());
// add some actual packages.

        instance.sentPackage(windowStart-10);
        instance.sentPackage(windowStart+ 1000 +150);
        instance.sentPackage(windowStart+3*1000+150);
        instance.sentPackage(windowStart+3*1000+151);
        instance.sentPackage(windowStart+3*1000+152);
        instance.sentPackage(windowStart+3*1000+153);
        instance.sentPackage(windowStart+5*1000+150);

        instance.respondedToPackage(windowStart+ 1000 +150, windowStart+ 1000 +450);
        instance.respondedToPackage(windowStart+3*1000+151, windowStart+3*1000+151+600);
        instance.respondedToPackage(windowStart+3*1000+153, windowStart+3*1000+153+602);

        //and now lets try:
        sentPackagePlot = instance.getSentPackagePlot(windowStart, windowEnd, 1);
        assertEquals(1, sentPackagePlot.getValues().get(2));
        assertEquals(4, sentPackagePlot.getValues().get(4));

        receivedPackagePlot=instance.getRespondedPackagePlots(windowStart, windowEnd, 1);
        assertEquals(1, receivedPackagePlot.getValues().get(2));
        assertEquals(2, receivedPackagePlot.getValues().get(4));

        delayPlot=instance.getResponseDelayPlot(windowStart, windowEnd, 1);
        assertEquals(300.0, delayPlot.getValues().get(2), 0.0001);
        assertEquals(601.0, delayPlot.getValues().get(4), 0.0001);

    }


    @Test
    void buildBuckets() {
        ZonedDateTime zt = ZonedDateTime.of(LocalDateTime.of(2020, 6, 1, 13, 30), ZoneId.of("UTC"));
        long roundMinute = zt.toInstant().toEpochMilli();
        List<Long> buckets = instance.buildBuckets(roundMinute, roundMinute + 30001, 1);
        assertEquals(LongStream.range(0, 32).map(i->roundMinute+i*1000).boxed().collect(Collectors.toList()),buckets);
    }
}