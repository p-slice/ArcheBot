package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Event;

public class PingEvent<B extends ArcheBot> extends Event<B> {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public PingEvent(B bot)
    {
        super(bot);
    }

    /*
     * =======================================
     * Internal classes:
     * =======================================
     */

    public static interface Listener<B extends ArcheBot> extends net.pslice.archebot.Listener<B>
    {
        public void onPing(PingEvent<B> event);
    }
}
