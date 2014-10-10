package net.pslice.archebot;

public class Event<B extends ArcheBot> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private final B bot;
    private final long timeStamp = System.currentTimeMillis();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    protected Event(B bot)
    {
        this.bot = bot;
    }

    /*
     * =======================================
     * Public methods:
     * =======================================
     */

    public B getBot()
    {
        return bot;
    }

    public long getTimeStamp()
    {
        return timeStamp;
    }
}
