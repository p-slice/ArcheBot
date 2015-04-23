package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;

public class RawEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final int ID;
    private final String line;

    public RawEvent(B bot, int ID, String line) {
        super(bot);

        this.ID = ID;
        this.line = line;
    }

    public int getID() {
        return ID;
    }

    public String getLine() {
        return line;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onLine(RawEvent<B> event);
    }
}
