package net.pslice.archebot.output;

import net.pslice.archebot.ArcheBot;

public final class NickMessage extends ArcheBot.Output {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public NickMessage(String nick)
    {
        super("NICK " + nick);
    }
}
