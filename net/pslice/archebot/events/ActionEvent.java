package net.pslice.archebot.events;

import net.pslice.archebot.*;

public class ActionEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private final Channel channel;
    private final User user;
    private final String action;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public ActionEvent(B bot, Channel channel, User user, String action)
    {
        super(bot);
        this.channel = channel;
        this.user = user;
        this.action = action;
    }

    /*
     * =======================================
     * Public methods:
     * =======================================
     */

    public Channel getChannel()
    {
        return channel;
    }

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

    public static interface Listener<B extends ArcheBot> extends ArcheBot.Listener<B>
    {
        public void onAction(ActionEvent<B> event);
    }
}
