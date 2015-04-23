package net.pslice.archebot.events;

import net.pslice.archebot.*;

public class ModeEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final Channel channel;
    private final User user;
    private final Mode mode;
    private final String value;
    private final boolean added;

    public ModeEvent(B bot, Channel channel, User user, Mode mode, String value, boolean added) {
        super(bot);

        this.channel = channel;
        this.user = user;
        this.mode = mode;
        this.value = value;
        this.added = added;
    }

    public Channel getChannel() {
        return channel;
    }

    public User getUser() {
        return user;
    }

    public Mode getMode() {
        return mode;
    }

    public String getValue() {
        return value;
    }

    public boolean modeAdded() {
        return added;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onModeSet(ModeEvent<B> event);
    }
}
