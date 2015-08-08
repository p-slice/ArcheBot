package net.pslice.archebot.output;

public final class NickservMessage extends Output {

    public NickservMessage(String nick, String password) {
        this(nick + " " + password);
    }

    public NickservMessage(String password) {
        super("NICKSERV IDENTIFY " + password);
    }
}
