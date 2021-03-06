package net.pslice.archebot.output;

import net.pslice.archebot.Server;

public final class VersionMessage extends Output {

    public VersionMessage() {
        super("VERSION");
    }

    public VersionMessage(Server server) {
        this(server.getName());
    }

    public VersionMessage(String server) {
        super("VERSION " + server);
    }
}
