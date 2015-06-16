package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;

public class RawEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final int ID;
    private final String[] args;
    private final String tail;

    public RawEvent(B bot, int ID, String[] args, String tail) {
        super(bot);

        this.ID = ID;
        this.args = args;
        this.tail = tail;
    }

    public int getID() {
        return ID;
    }

    public String[] getArgs() {
        return args;
    }

    public String getTail() {
        return tail;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onLine(RawEvent<B> event);
    }
}
