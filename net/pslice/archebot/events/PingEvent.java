package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;

public class PingEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

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

    public static interface Listener<B extends ArcheBot> extends ArcheBot.Listener<B>
    {
        public void onPing(PingEvent<B> event);
    }
}
