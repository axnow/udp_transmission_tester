package xyz.nowinski.udptester;

import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.isNull;

@Slf4j
public class PackageTracker {
    private TreeMap<Long, Package> packages = new TreeMap<>();


    public void sentPackage(long timestamp) {
        packages.put(timestamp, new Package(timestamp));
    }

    public void respondedToPackage(long requestPackageTimestamp, long responseReceivedTimestamp) {
        if (isNull(packages.computeIfPresent(requestPackageTimestamp,
                (reqTimestamp, pkg) -> new Package(requestPackageTimestamp, responseReceivedTimestamp)))) {
            log.warn(
                    "Got response, but source package is not present in map. Request timestamp={}, response received timestamp={}",
                    requestPackageTimestamp, responseReceivedTimestamp);
        }

    }

    public PlotData<Integer> getSentPackagePlot(long from, long to, int bucketWidth) {
        PlotData<Integer> input = prepareData(from, to,
                bucketWidth, (i, p) -> Optional.ofNullable(i).orElse(0) + 1, null);
        return new PlotData<>(input.getPoints(),
                input.getValues()
                        .stream()
                        .map(i -> Optional.ofNullable(i).orElse(0))
                        .collect(Collectors.toList()));
    }


    public PlotData<Integer> getRespondedPackagePlots(long from, long to, int bucketWidth) {
        PlotData<Integer> input = prepareData(from, to, bucketWidth, (i, p) -> Optional.ofNullable(i).orElse(0) + 1,
                Package::isReplayed);
        return new PlotData<>(input.getPoints(),
                input.getValues().stream().map(i -> Optional.ofNullable(i).orElse(0)).collect(
                        Collectors.toList()));
    }

    public PlotData<Double> getResponseDelayPlot(long from, long to, int bucketWidth) {
        PlotData<LongSummaryStatistics> input = prepareData(from, to,
                bucketWidth, (ds, p) -> {
                    LongSummaryStatistics stat = Optional.ofNullable(ds).orElse(new LongSummaryStatistics());
                    stat.accept(p.getResponseDelay());
                    return stat;
                }, Package::isReplayed);
        return new PlotData<>(input.getPoints(),
                input.getValues()
                        .stream()
                        .map(i -> Optional.ofNullable(i).map(LongSummaryStatistics::getAverage).orElse(null))
                        .collect(Collectors.toList()));
    }

    private <T> PlotData<T> prepareData(long from, long to, int bucketWidth,
                                        BiFunction<T, Package, T> aggregator, Predicate<Package> filter) {
        List<Long> buckets = buildBuckets(from, to, bucketWidth);
        Map<Integer, T> values = new HashMap<>();
        packages.values()
                .stream()
                .filter(p -> packageInScope(buckets, p))
                .filter(p -> Optional.ofNullable(filter).map(f -> f.test(p)).orElse(true))
                .forEach(p -> values.compute(findBucket(buckets, p), (i, v) -> aggregator.apply(v, p)));

        return new PlotData<T>(buckets, IntStream.range(0, buckets.size())
                .mapToObj(values::get)
                .collect(Collectors.toList()));

    }

    private int findBucket(List<Long> buckets, Package p) {
        int res = 0;
        for (Long t : buckets) {
            if (p.getSendTimestamp() <= t) {
                return res;
            }
            res++;
        }
        return res;
    }

    private boolean packageInScope(List<Long> buckets, Package p) {
        return p.getSendTimestamp() >= buckets.get(0) && p.getSendTimestamp() <= buckets.get(buckets.size() - 1);
    }


    protected List<Long> buildBuckets(long from, long to, int bucketWidth) {
        ZonedDateTime startTime = new Date(from).toInstant().atZone(ZoneId.systemDefault());
        ZonedDateTime endTime = new Date(to).toInstant().atZone(ZoneId.systemDefault());
        startTime = startTime.withSecond(startTime.getSecond() / bucketWidth);
        List<Long> buckets = new ArrayList<>();

        ZonedDateTime currentTime = startTime;
        buckets.add(currentTime.toInstant().toEpochMilli());
        do {
            currentTime = currentTime.plusSeconds(bucketWidth);
            buckets.add(currentTime.toInstant().toEpochMilli());

        } while (currentTime.isBefore(endTime));
        return buckets;
    }

    Long lastResponse() {
        Optional<Package> lastReplayed = packages.values().stream().filter(p -> p.isReplayed())
                .max(Comparator.comparing(Package::getSendTimestamp));
        return lastReplayed.map(Package::getSendTimestamp).orElse(null);
    }

    public int getSentPackages() {
        return packages.size();
    }

    public int getRespondedPackages() {
        return (int) packages.values().stream().filter(Package::isReplayed).count();
    }

    public Long getFirstPackageTimestamp() {
        return packages.values().stream().min(Comparator.comparing(Package::getSendTimestamp))
                .map(Package::getSendTimestamp).orElse(null);
    }

    public Long getLastPackageTimestamp() {
        return packages.values().stream()
                .max(Comparator.comparing(Package::getSendTimestamp))
                .map(Package::getSendTimestamp)
                .orElse(null);
    }

    public Long getLastRespondedPackageTimestamp() {
        return packages.values().stream()
                .filter(Package::isReplayed)
                .max(Comparator.comparing(Package::getSendTimestamp))
                .map(Package::getSendTimestamp)
                .orElse(null);
    }

}
