package xyz.nowinski.udptester;

import lombok.Value;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Value
public class Package {
    Long sendTimestamp;
    Long responseTimestamp;


    public Package(Long sendTimestamp, Long responseTimestamp) {
        this.sendTimestamp = sendTimestamp;
        this.responseTimestamp = responseTimestamp;
    }

    public Package(long timestamp) {
        this.sendTimestamp = timestamp;
        responseTimestamp = null;
    }


    public ZonedDateTime getSendTime() {
        return new Date(sendTimestamp).toInstant().atZone(ZoneId.systemDefault());
    }

    public Long getResponseDelay() {
        return Optional.ofNullable(responseTimestamp).map(e -> e - sendTimestamp).orElse(null);
    }

    public boolean isReplayed() {
        return nonNull(responseTimestamp);
    }

    @Override
    public String toString() {
        return "Package{" +
                "sendTimestamp=" + sendTimestamp +
                ", responseTimestamp=" + responseTimestamp + ", delay=" + getResponseDelay() +
                '}';
    }
}
