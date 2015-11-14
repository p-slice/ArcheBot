package net.pslice.archebot;

import java.util.TreeMap;
import java.util.TreeSet;

public class UserMap {

    private final ArcheBot bot;
    private final TreeMap<String, User> users = new TreeMap<>();

    UserMap(ArcheBot bot) {
        this.bot = bot;
    }

    public User getUser(String nick) {
        if (nick.isEmpty())
            return new User();
        if (!isUser(nick)) {
            User user = new User(nick);
            addUser(user);
            bot.updatePermissions(user);
        }
        return users.get(nick.toLowerCase());
    }

    public TreeSet<String> getUserNicks() {
        return new TreeSet<>(users.keySet());
    }

    public TreeSet<User> getUsers() {
        return new TreeSet<>(users.values());
    }

    public boolean isUser(String nick) {
        return users.containsKey(nick.toLowerCase());
    }

    void addUser(User user) {
        users.put(user.nick.toLowerCase(), user);
    }

    void removeUser(String nick) {
        if (isUser(nick))
            bot.savePermissions(getUser(nick));
        users.remove(nick.toLowerCase());
    }
}
