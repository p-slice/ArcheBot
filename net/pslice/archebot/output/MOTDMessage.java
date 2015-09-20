package net.pslice.archebot.output;

import net.pslice.archebot.Server;

public final class MOTDMessage extends Output {

    public MOTDMessage() {
        super("MOTD");
    }

    public MOTDMessage(Server server) {
        this(server.getName());
    }

    public MOTDMessage(String server) {
        super("MOTD " + server);
    }
}
