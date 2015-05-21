package net.pslice.archebot;

import net.pslice.utilities.StringUtils;

import java.util.HashMap;
import java.util.HashSet;

public class Channel implements Comparable<Channel> {

    public final String name;
    protected String topic = "", topicSetter = "";
    protected final HashMap<User, HashSet<Mode>> users = new HashMap<>();
    protected final HashMap<Mode, String> modes = new HashMap<>();
    protected final HashMap<Mode, HashSet<String>> listModes = new HashMap<>();

    protected Channel(String name) {
        this.name = name;
    }

    public boolean contains(User user) {
        return users.containsKey(user);
    }

    public String details() {
        return name + (modes.size() > 0 ? " {MODES:" + StringUtils.compact(modes.keySet(), "") + "}" : "") +
                " {TOTAL USERS:" + totalUsers() + "} {TOPIC:" + topic + "}";
    }

    public String getValue(Mode mode) {
        return modes.containsKey(mode) ? modes.get(mode) : null;
    }

    public HashSet<String> getValues(Mode mode) {
        return listModes.containsKey(mode) ? listModes.get(mode) : null;
    }

    public boolean isValue(Mode mode, String value) {
        return listModes.containsKey(mode) && listModes.get(mode).contains(value);
    }

    public HashSet<Mode> getModes() {
        return new HashSet<>(modes.keySet());
    }

    public HashSet<Mode> getModes(User user) {
        return users.containsKey(user) ? new HashSet<>(users.get(user)) : null;
    }

    public String getName() {
        return name;
    }

    public String getTopic() {
        return topic;
    }

    public String getTopicSetter() {
        return topicSetter;
    }

    public HashSet<User> getUsers() {
        return new HashSet<>(users.keySet());
    }

    public HashSet<User> getUsers(Mode mode) {
        HashSet<User> modeUsers = new HashSet<>();
        for (User user : users.keySet())
            if (users.get(user).contains(mode))
                modeUsers.add(user);
        return modeUsers;
    }

    public boolean hasMode(Mode mode) {
        return modes.containsKey(mode);
    }

    public boolean hasMode(User user, Mode mode) {
        return users.containsKey(user) && users.get(user).contains(mode);
    }

    public int totalUsers() {
        return users.size();
    }

    public int totalUsers(Mode mode) {
        int size = 0;
        for (User user : users.keySet())
            if (hasMode(user, mode))
                size++;
        return size;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Channel channel) {
        return name.compareToIgnoreCase(channel.name);
    }

    @Override
    public String toString() {
        return name;
    }

    void addMode(User user, Mode mode) {
        if (users.containsKey(user) && !users.get(user).contains(mode))
            users.get(user).add(mode);
    }

    void addMode(Mode mode, String value) {
        if (mode.isList()) {
            if (!listModes.containsKey(mode))
                listModes.put(mode, new HashSet<String>());
            listModes.get(mode).add(value);
        } else if (mode.isValue())
            modes.put(mode, value);
    }

    void addUser(User user) {
        users.put(user, new HashSet<Mode>());
    }

    void removeMode(User user, Mode mode) {
        if (users.containsKey(user) && users.get(user).contains(mode))
            users.get(user).remove(mode);
    }

    void removeMode(Mode mode, String value) {
        if (mode.isList() && listModes.containsKey(mode))
            listModes.get(mode).remove(value);
        else if (mode.isValue())
            modes.remove(mode);
    }

    void removeUser(User user) {
        users.remove(user);
    }

    void setTopic(String topic) {
        this.topic = topic;
    }

    void setTopicSetter(String topicSetter) {
        this.topicSetter = topicSetter;
    }
}
