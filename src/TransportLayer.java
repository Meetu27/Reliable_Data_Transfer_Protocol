import java.util.*;

public abstract class TransportLayer {
    public static final int MAX_SEQ = 1;
    public static final int EVENT_PACKET_ARRIVAL = 0;
    public static final int EVENT_TIMEOUT = 1;
    public static final int EVENT_MESSAGE_TOSEND = 2;
    public static final long TIMEOUT = 500; // in millisecond
    public static final int LOSSY_CHANNEL_RATE = 3;

    java.util.Timer m_timer;
    SendTimerTask m_timerTask = new SendTimerTask();

    // state variable to indicate if we should process an event
    boolean m_wakeup = false;

    // state variable to indicate the type of event that occurred
    int m_event = -1;

    LossyChannel m_lossyChannel = null;

    public TransportLayer(LossyChannel lc) {
        m_lossyChannel = lc;
    }

    // to be implemented by sub-classes for actual sending side
    // and receiving side protocol
    public abstract void run();

    // simulate 1-bit sequence increment operation
    byte increment(byte seq) {
        byte newseq;
        if(seq < MAX_SEQ)
            newseq = 1;
        else // seq number wrap around
            newseq = 0;
        return newseq;
    }

    void sendToLossyChannel(Packet p) {
        m_lossyChannel.send(p.toBytes());
    }

    Packet receiveFromLossyChannel() {
        byte[] receivedData = m_lossyChannel.receive();
        Packet packet = new Packet(receivedData);
        return packet;
    }

    void startTimer() {
        try {
            m_timer = new java.util.Timer();
            m_timer.schedule(new SendTimerTask(), TIMEOUT);
        } catch(Exception e) {}
    }

    void stopTimer() {
        try {
            m_timer.cancel();
        } catch(Exception e) {}
    }

    public synchronized int waitForEvent() {
        while(!m_wakeup) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
        m_wakeup = false;
        return m_event;
    }

    public synchronized void onPacketArrival() {
        m_wakeup = true;
        m_event = EVENT_PACKET_ARRIVAL;
        notifyAll();
    }

    public synchronized void onTimeout() {
        m_wakeup = true;
        m_event = EVENT_TIMEOUT;
        notifyAll();
    }

    public class SendTimerTask extends TimerTask {
        public void run() {
            onTimeout();
        }
    }
}