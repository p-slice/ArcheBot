package net.pslice.archebot.output;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Server;

public final class VersionMessage extends ArcheBot.Output {

    public VersionMessage() {
        super("VERSION");
    }

    public VersionMessage(Server server) {
        this(server.name);
    }

    public VersionMessage(String server) {
        super("VERSION " + server);
    }
}
