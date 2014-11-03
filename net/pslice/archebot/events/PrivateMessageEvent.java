package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.User;

public class PrivateMessageEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private final User user;
    private final String message;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public PrivateMessageEvent(B bot, User user, String message)
    {
        super(bot);

        this.user = user;
        this.message = message;
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

    public String getMessage()
    {
        return message;
    }

    /*
     * =======================================
     * Internal classes:
     * =======================================
     */

    public static interface Listener<B extends ArcheBot> extends ArcheBot.Listener<B>
    {
        public void onPrivateMessage(PrivateMessageEvent<B> event);
    }
}
