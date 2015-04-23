package net.pslice.archebot;

import net.pslice.utilities.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Channel implements Comparable<Channel> {

    public final String name;
    protected String topic = "", topicSetter = "";
    protected final HashMap<User, Set<Mode.TempMode>> users = new HashMap<>();
    protected final HashMap<Mode.ValueMode, String> modes = new HashMap<>();
    protected final HashMap<Mode.PermaMode, Set<String>> permaModes = new HashMap<>();

    protected Channel(String name) {
        this.name = name;
        for (Mode mode : Mode.getModes())
            if (mode instanceof Mode.PermaMode)
                permaModes.put((Mode.PermaMode) mode, new HashSet<String>());
    }

    public boolean contains(User user) {
        return users.containsKey(user);
    }

    public String details() {
        String modeUsers = "";
        for (Mode mode : Mode.getModes())
            if (mode instanceof Mode.TempMode)
                if (totalUsers(mode) > 0)
                    modeUsers += " {" + mode + ":" + totalUsers(mode) + "}";
        return name + (modes.size() > 0 ? " {MODES:" + StringUtils.compact(modes.keySet(), "") + "}" : "") +
                modeUsers + " {TOTAL USERS:" + totalUsers() + "} {TOPIC:" + topic + "}";
    }

    public String getValue(Mode mode) {
        if (!(mode instanceof Mode.ValueMode))
            return null;
        return modes.containsKey(mode) ? modes.get(mode) : "";
    }

    public Set<String> getValues(Mode mode) {
        if (!(mode instanceof Mode.PermaMode))
            return null;
        return permaModes.get(mode);
    }

    public boolean isValue(Mode mode, String value) {
        return mode instanceof Mode.PermaMode && permaModes.get(mode).contains(value);
    }

    public HashSet<Mode.ValueMode> getModes() {
        return new HashSet<>(modes.keySet());
    }

    public HashSet<Mode.TempMode> getModes(User user) {
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

    public Set<User> getUsers() {
        return new HashSet<>(users.keySet());
    }

    public HashSet<User> getUsers(Mode mode) {
        if (!(mode instanceof Mode.TempMode))
            return null;
        HashSet<User> modeUsers = new HashSet<>();
        for (User user : users.keySet())
            if (users.get(user).contains(mode))
                modeUsers.add(user);

        return modeUsers;
    }

    public boolean hasMode(Mode mode) {
        return mode instanceof Mode.ValueMode && modes.containsKey(mode);
    }

    public boolean hasMode(User user, Mode mode) {
        return mode instanceof Mode.TempMode && users.containsKey(user) && users.get(user).contains(mode);
    }

    public int totalUsers() {
        return users.size();
    }

    public int totalUsers(Mode mode) {
        if (!(mode instanceof Mode.TempMode))
            return 0;
        int size = 0;
        for (User user : users.keySet())
            if (this.hasMode(user, mode))
                size++;
        return size;
    }

    @Override
    public String toString() {
        return name;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Channel channel) {
        return name.compareToIgnoreCase(channel.name);
    }

    void addUser(User user) {
        users.put(user, new HashSet<Mode.TempMode>());
    }

    void removeUser(User user) {
        users.remove(user);
    }

    void addMode(User user, Mode.TempMode mode) {
        if (users.containsKey(user) && !users.get(user).contains(mode))
            users.get(user).add(mode);
    }

    void removeMode(User user, Mode.TempMode mode) {
        if (users.containsKey(user) && users.get(user).contains(mode))
            users.get(user).remove(mode);
    }

    void addMode(Mode mode, String value) {
        if (mode instanceof Mode.ValueMode)
            modes.put((Mode.ValueMode) mode, value);
        else if (mode instanceof Mode.PermaMode)
            permaModes.get(mode).add(value);
    }

    void removeMode(Mode mode, String value) {
        if (mode instanceof Mode.ValueMode)
            modes.remove(mode);
        else if (mode instanceof Mode.PermaMode)
            permaModes.get(mode).remove(value);
    }

    void setTopic(String topic) {
        this.topic = topic;
    }

    void setTopicSetter(String topicSetter) {
        this.topicSetter = topicSetter;
    }
}
