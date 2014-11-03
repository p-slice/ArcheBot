package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.User;

public class NickEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private final User user;
    private final String oldNick;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public NickEvent(B bot, User user, String oldNick)
    {
        super(bot);

        this.user = user;
        this.oldNick = oldNick;
    }

    /*
     * =======================================
     * Public methods:
     * =======================================
     */

    public User getUser()
    {
        return user;
    }

    public String getOldNick()
    {
        return oldNick;
    }

    /*
     * =======================================
     * Internal classes:
     * =======================================
     */

    public static interface Listener<B extends ArcheBot> extends ArcheBot.Listener<B>
    {
        public void onNickChange(NickEvent<B> event);
    }
}
