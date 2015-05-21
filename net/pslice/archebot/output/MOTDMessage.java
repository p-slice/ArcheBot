package net.pslice.archebot.output;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Server;

public class MOTDMessage extends ArcheBot.Output {

    public MOTDMessage() {
        super("MOTD");
    }

    public MOTDMessage(Server server) {
        this(server.name);
    }

    public MOTDMessage(String server) {
        super("MOTD " + server);
    }
}
