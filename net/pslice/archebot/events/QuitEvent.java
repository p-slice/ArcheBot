package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Event;
import net.pslice.archebot.User;

public class QuitEvent<B extends ArcheBot> extends Event<B> {

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

    public QuitEvent(B bot, User user, String message)
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

    public static interface Listener<B extends ArcheBot> extends net.pslice.archebot.Listener<B>
    {
        public void onQuit(QuitEvent<B> event);
    }
}
