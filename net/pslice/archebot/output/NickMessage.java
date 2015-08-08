package net.pslice.archebot.output;

public final class NickMessage extends Output {
    public NickMessage(String nick) {
        super("NICK " + nick);
    }
}
