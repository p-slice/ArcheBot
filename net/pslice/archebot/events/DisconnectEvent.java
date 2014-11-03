package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;

public class DisconnectEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private final String message;
    private final boolean reconnect;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public DisconnectEvent(B bot, String message, boolean reconnect)
    {
        super(bot);

        this.message = message;
        this.reconnect = reconnect;
    }

    /*
     * =======================================
     * Public methods:
     * =======================================
     */

    public String getMessage()
    {
        return message;
    }

    public boolean getReconnect()
    {
        return reconnect;
    }

    /*
     * =======================================
     * Internal classes:
     * =======================================
     */

    public static interface Listener<B extends ArcheBot> extends ArcheBot.Listener<B>
    {
        public void onDisconnect(DisconnectEvent<B> event);
    }
}
