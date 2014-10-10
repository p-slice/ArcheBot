package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Event;

public class ConnectEvent<B extends ArcheBot> extends Event<B> {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public ConnectEvent(B bot)
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
        public void onConnect(ConnectEvent<B> event);
    }
}
