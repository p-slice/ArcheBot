package net.pslice.archebot.output;

import net.pslice.archebot.ArcheBot;

public final class NickservMessage extends ArcheBot.Output {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public NickservMessage(String nick, String password)
    {
        this(nick + " " + password);
    }

    public NickservMessage(String password)
    {
        super("NICKSERV IDENTIFY " + password);
    }
}
