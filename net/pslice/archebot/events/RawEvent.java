package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;

public class RawEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private final int ID;
    private final String line;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public RawEvent(B bot, int ID, String line)
    {
        super(bot);

        this.ID = ID;
        this.line = line;
    }

    /*
     * =======================================
     * Public methods:
     * =======================================
     */

    public int getID()
    {
        return ID;
    }

    public String getLine()
    {
        return line;
    }

    /*
     * =======================================
     * Internal classes:
     * =======================================
     */

    public static interface Listener<B extends ArcheBot> extends ArcheBot.Listener<B>
    {
        public void onLine(RawEvent<B> event);
    }
}
