package xyz.nowinski.udptester;

public interface PackageListener {
    void packageSent(long timestamp);
    void packageReceived(long sentTimestamp, long receivedTimesamp);

}
