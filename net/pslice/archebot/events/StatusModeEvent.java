package net.pslice.archebot.events;

import net.pslice.archebot.*;

public class StatusModeEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final Channel channel;
    private final User setter, receiver;
    private final Mode mode;
    private final boolean added;

    public StatusModeEvent(B bot, Channel channel, User setter, User receiver, Mode mode, boolean added) {
        super(bot);

        this.channel = channel;
        this.setter = setter;
        this.receiver = receiver;
        this.mode = mode;
        this.added = added;
    }

    public Channel getChannel() {
        return channel;
    }

    public User getSetter() {
        return setter;
    }

    public User getReceiver() {
        return receiver;
    }

    public Mode getMode() {
        return mode;
    }

    public boolean modeAdded() {
        return added;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onStatusMode(StatusModeEvent<B> event);
    }
}
