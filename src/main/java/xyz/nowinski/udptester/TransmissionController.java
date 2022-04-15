package xyz.nowinski.udptester;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.EventListener;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Slf4j
public class TransmissionController {
    private static final long CONNECTION_TIMEOUT = 1000L;
    @Getter @Setter
    int port;
    @Getter @Setter
    String serverAddress;
    private UDPConnector connector=null;

    int pkgPerSecond;
    private ChangeListener eventListener;
    @Getter
    private PackageTracker packageTracker = new PackageTracker();

    boolean running=false;

    public boolean isRunning() {
        return running;
    }



    public void start()  {
        log.info("Starting transmission...");
        running=true;
        log.info("Connecting to {} {}", serverAddress, port);
        connector=new UDPConnector(port, serverAddress);
        connector.addPackageListener(new PackageListener() {
            @Override
            public void packageSent(long timestamp) {
                packageTracker.sentPackage(timestamp);
            }

            @Override
            public void packageReceived(long sentTimestamp, long receivedTimesamp) {
                packageTracker.respondedToPackage(sentTimestamp, receivedTimesamp);
            }
        });
        try {
            connector.startup();
        } catch (Exception e) {
            log.error("Failed to start connector...", e);
        }
        eventListener.stateChanged(new ChangeEvent(this));
    }

    public void stop() {
        log.info("Stoping transmission...");
        running=false;
        if(connector!=null) {
            connector.shutdown();
            connector=null;
        }
        eventListener.stateChanged(new ChangeEvent(this));

    }

    public boolean isConnected() {
        return running && Optional.ofNullable(packageTracker.lastResponse()).map(t->System.currentTimeMillis()-t<CONNECTION_TIMEOUT).orElse(false);
    }

    public void addEventListener(ChangeListener l) {
        this.eventListener = l;
    }

    public void setPkgPerSec(Integer value) {
        if(nonNull(connector)&& nonNull(value)) {
            connector.setMessageDelay(1000/value);
        }
    }
}
