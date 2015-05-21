package net.pslice.archebot.output;

import net.pslice.archebot.ArcheBot;

public final class Ping extends ArcheBot.Output {

    public Ping() {
        this("" + System.currentTimeMillis());
    }

    public Ping(String message) {
        super("PING :" + message);
    }
}
