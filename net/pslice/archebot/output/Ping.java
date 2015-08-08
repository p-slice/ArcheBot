package net.pslice.archebot.output;

public final class Ping extends Output {

    public Ping() {
        this("" + System.currentTimeMillis());
    }

    public Ping(String message) {
        super("PING :" + message);
    }
}
