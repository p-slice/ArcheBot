package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;

public class CodeEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final int code;
    private final String[] args;
    private final String tail;

    public CodeEvent(B bot, int code, String[] args, String tail) {
        super(bot);

        this.code = code;
        this.args = args;
        this.tail = tail;
    }

    public int getCode() {
        return code;
    }

    public String[] getArgs() {
        return args;
    }

    public String getTail() {
        return tail;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onLine(CodeEvent<B> event);
    }
}
