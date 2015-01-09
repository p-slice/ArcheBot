package net.pslice.archebot.output;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.User;

public final class ErrorMessage extends ArcheBot.Output {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public ErrorMessage(User user, String error, Object... objects)
    {
        this(user.getNick(), error, objects);
    }

    public ErrorMessage(String target, String error, Object... objects)
    {
        super("NOTICE " + target + " :&b&4Error&r: " + String.format(error, objects));
    }
}
