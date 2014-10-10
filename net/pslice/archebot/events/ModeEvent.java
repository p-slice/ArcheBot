package net.pslice.archebot.events;

import net.pslice.archebot.*;

public class ModeEvent<B extends ArcheBot> extends Event<B> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private final Channel channel;
    private final User user;
    private final Mode mode;
    private final String value;
    private final boolean added;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public ModeEvent(B bot, Channel channel, User user, Mode mode, String value, boolean added)
    {
        super(bot);

        this.channel = channel;
        this.user = user;
        this.mode = mode;
        this.value = value;
        this.added = added;
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

    public Mode getMode()
    {
        return mode;
    }

    public String getValue()
    {
        return value;
    }

    public boolean modeAdded()
    {
        return added;
    }

    /*
     * =======================================
     * Internal classes:
     * =======================================
     */

    public static interface Listener<B extends ArcheBot> extends net.pslice.archebot.Listener<B>
    {
        public void onModeSet(ModeEvent<B> event);
    }
}
