package net.pslice.archebot;

import java.util.HashMap;
import java.util.HashSet;

public class Channel implements Comparable<Channel> {

    protected final String name;
    protected final HashMap<User, HashSet<Character>> users = new HashMap<>();
    protected final HashMap<Character, String> modes = new HashMap<>();
    protected final HashMap<Character, HashSet<String>> listModes = new HashMap<>();
    protected String topic = "", topicSetter = "";
    protected long topicTimestamp = -1;

    protected Channel(String name) {
        this.name = name;
    }

    public boolean contains(User user) {
        return users.containsKey(user);
    }

    public HashSet<Character> getListModes() {
        return new HashSet<>(listModes.keySet());
    }

    public HashSet<Character> getModes() {
        return new HashSet<>(modes.keySet());
    }

    public HashSet<Character> getModes(User user) {
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

    public long getTopicTimestamp() {
        return topicTimestamp;
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

    public String getValue(char mode) {
        return modes.containsKey(mode) ? modes.get(mode) : null;
    }

    public HashSet<String> getValues(char mode) {
        return listModes.containsKey(mode) ? listModes.get(mode) : null;
    }

    public boolean hasMode(char mode) {
        return modes.containsKey(mode);
    }

    public boolean hasMode(User user, char mode) {
        return users.containsKey(user) && users.get(user).contains(mode);
    }

    public boolean isValue(char mode, String value) {
        return listModes.containsKey(mode) && listModes.get(mode).contains(value);
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
}
