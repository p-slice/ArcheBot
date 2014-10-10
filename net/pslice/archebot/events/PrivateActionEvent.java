package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Event;
import net.pslice.archebot.User;

public class PrivateActionEvent<B extends ArcheBot> extends Event<B> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private final User user;
    private final String action;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public PrivateActionEvent(B bot, User user, String action)
    {
        super(bot);

        this.user = user;
        this.action = action;
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

    public String getAction()
    {
        return action;
    }

    /*
     * =======================================
     * Internal classes:
     * =======================================
     */

    public static interface Listener<B extends ArcheBot> extends net.pslice.archebot.Listener<B>
    {
        public void onPrivateAction(PrivateActionEvent<B> event);
    }
}
