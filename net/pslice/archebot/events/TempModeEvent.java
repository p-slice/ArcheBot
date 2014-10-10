package net.pslice.archebot.events;

import net.pslice.archebot.*;

public class TempModeEvent<B extends ArcheBot> extends Event<B> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private final Channel channel;
    private final User setter,
                       receiver;
    private final Mode.TempMode mode;
    private final boolean added;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public TempModeEvent(B bot, Channel channel, User setter, User receiver, Mode.TempMode mode, boolean added)
    {
        super(bot);

        this.channel = channel;
        this.setter = setter;
        this.receiver = receiver;
        this.mode = mode;
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

    public User getSetter()
    {
        return setter;
    }

    public User getReceiver()
    {
        return receiver;
    }

    public Mode.TempMode getMode()
    {
        return mode;
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
        public void onTempModeSet(TempModeEvent<B> event);
    }
}
