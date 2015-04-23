package net.pslice.archebot.events;

import net.pslice.archebot.*;

public class ActionEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    private final Channel channel;
    private final User user;
    private final String action;

    public ActionEvent(B bot, Channel channel, User user, String action) {
        super(bot);

        this.channel = channel;
        this.user = user;
        this.action = action;
    }

    public Channel getChannel() {
        return channel;
    }

    public User getUser() {
        return user;
    }

    public String getAction() {
        return action;
    }

    public interface Handler<B extends ArcheBot> extends ArcheBot.Handler<B> {
        void onAction(ActionEvent<B> event);
    }
}
