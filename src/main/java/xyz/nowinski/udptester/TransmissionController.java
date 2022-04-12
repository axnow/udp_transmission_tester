package xyz.nowinski.udptester;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.time.LocalDateTime;
import java.util.EventListener;

@Slf4j
public class TransmissionController {
    @Getter @Setter
    int port;
    @Getter @Setter
    String serverAddress;
    UDPConnector connector=null;

    int pkgPerSecond;
    private ChangeListener eventListener;

    boolean running=false;

    public boolean isRunning() {
        return running;
    }



    public void start()  {
        log.info("Starting transmission...");
        running=true;
        log.info("Connecting to {} {}", serverAddress, port);
        connector=new UDPConnector(port, serverAddress);
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
        return running && (LocalDateTime.now().getSecond()/10)%2!=0;
    }

    public void addEventListener(ChangeListener l) {
        this.eventListener = l;
    }
}
