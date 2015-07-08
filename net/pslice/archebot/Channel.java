package net.pslice.archebot;

import net.pslice.utilities.StringUtils;

import java.util.HashMap;
import java.util.HashSet;

public class Channel implements Comparable<Channel> {

    public final String name;
    protected final HashMap<User, HashSet<Character>> users = new HashMap<>();
    protected final HashMap<Character, String> modes = new HashMap<>();
    protected final HashMap<Character, HashSet<String>> listModes = new HashMap<>();
    protected String topic = "", topicSetter = "";

    protected Channel(String name) {
        this.name = name;
    }

    public boolean contains(User user) {
        return users.containsKey(user);
    }

    public String details() {
        return String.format("%s {MODES:%s} {USERS:%d} {TOPIC:%s}",
                name, StringUtils.compact(modes.keySet(), ""), users.size(), topic);
    }

    public String getValue(char mode) {
        return modes.containsKey(mode) ? modes.get(mode) : null;
    }

    public HashSet<String> getValues(char mode) {
        return listModes.containsKey(mode) ? listModes.get(mode) : null;
    }

    public boolean isValue(char mode, String value) {
        return listModes.containsKey(mode) && listModes.get(mode).contains(value);
    }

    public HashSet<Character> getModes() {
        return new HashSet<>(modes.keySet());
    }

    public HashSet<Character> getModes(User user) {
        return users.containsKey(user) ? new HashSet<>(users.get(user)) : null;
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

    public HashSet<User> getUsers(char mode) {
        HashSet<User> modeUsers = new HashSet<>();
        for (User user : users.keySet())
            if (users.get(user).contains(mode))
                modeUsers.add(user);
        return modeUsers;
    }

    public boolean hasMode(char mode) {
        return modes.containsKey(mode);
    }

    public boolean hasMode(User user, char mode) {
        return users.containsKey(user) && users.get(user).contains(mode);
    }

    public int totalUsers() {
        return users.size();
    }

    public int totalUsers(char mode) {
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

    void addMode(User user, char mode) {
        if (users.containsKey(user) && !users.get(user).contains(mode))
            users.get(user).add(mode);
    }

    void addListMode(char mode, String value) {
        if (!listModes.containsKey(mode))
            listModes.put(mode, new HashSet<String>());
        listModes.get(mode).add(value);
    }

    void addMode(char mode, String value) {
        modes.put(mode, value);
    }

    void addUser(User user) {
        users.put(user, new HashSet<Character>());
    }

    void removeMode(User user, char mode) {
        if (users.containsKey(user) && users.get(user).contains(mode))
            users.get(user).remove(mode);
    }

    void removeListMode(char mode, String value){
        if (listModes.containsKey(mode)) {
            listModes.get(mode).remove(value);
            if (listModes.get(mode).size() == 0)
                listModes.remove(mode);
        }
    }

    void removeMode(char mode) {
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
