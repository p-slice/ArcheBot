package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;

public class ConnectEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

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

    public static interface Listener<B extends ArcheBot> extends ArcheBot.Listener<B>
    {
        public void onConnect(ConnectEvent<B> event);
    }
}
