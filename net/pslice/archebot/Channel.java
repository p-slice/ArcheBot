package net.pslice.archebot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Channel {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    // The name of the channel
    public final String name;

    // Information about the channel topic
    private String topic       = "",
                   topicSetter = "";

    // All users in the channel, and their ranks
    private final HashMap<User, Set<User.Mode>> users = new HashMap<>();

    // The current modes set on the channel
    private final Set<Channel.Mode> modes = new HashSet<>();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    Channel(String name)
    {
        this.name = name;
    }

    /*
     * =======================================
     * Public methods:
     * =======================================
     */

    public boolean contains(User user)
    {
        return users.containsKey(user);
    }

    public Set<Channel.Mode> getModes()
    {
        return new HashSet<>(modes);
    }

    public Set<User.Mode> getModes(User user)
    {
        return users.containsKey(user) ? new HashSet<>(users.get(user)) : null;
    }

    public String getName()
    {
        return name;
    }

    public String getTopic()
    {
        return topic;
    }

    public String getTopicSetter()
    {
        return topicSetter;
    }

    public Set<User> getUsers()
    {
        return new HashSet<>(users.keySet());
    }

    public Set<User> getUsers(User.Mode mode)
    {
        Set<User> modeUsers = new HashSet<>();

        for (User user : users.keySet())
            if (users.get(user).contains(mode))
                modeUsers.add(user);

        return modeUsers;
    }

    public boolean hasMode(Channel.Mode mode)
    {
        return modes.contains(mode);
    }

    public boolean hasMode(User user, User.Mode mode)
    {
        return users.containsKey(user) && users.get(user).contains(mode);
    }

    public int totalUsers()
    {
        return users.size();
    }

    public int totalUsers(User.Mode mode)
    {
        int size = 0;
            for (User user : users.keySet())
                if (this.hasMode(user, mode))
                    size++;
        return size;
    }

    /*
     * =======================================
     * Overridden methods:
     * =======================================
     */

    @Override
    public String toString()
    {
        return name;
    }

    /*
     * =======================================
     * Local methods:
     * =======================================
     */

    void addUser(User user)
    {
        users.put(user, new HashSet<User.Mode>());
    }

    void removeUser(User user)
    {
        users.remove(user);
    }

    void addMode(User user, User.Mode mode)
    {
        users.get(user).add(mode);
    }

    void removeMode(User user, User.Mode mode)
    {
        users.get(user).remove(mode);
    }

    void addMode(Channel.Mode mode)
    {
        modes.add(mode);
    }

    void removeMode(Channel.Mode mode)
    {
        modes.remove(mode);
    }

    void setTopic(String topic)
    {
        this.topic = topic;
    }

    void setTopicSetter(String topicSetter)
    {
        this.topicSetter = topicSetter;
    }

    /*
     * =======================================
     * Internal classes:
     * =======================================
     */

    public static enum Mode
    {
        /*
         * =======================================
         * Enum values:
         * =======================================
         */

        moderated('m'),
        invite('i'),
        topicProtection('t'),
        hidden('p'),
        secret('s'),
        noExternalMessages('n');

        /*
         * =======================================
         * Objects and variables:
         * =======================================
         */

        private final char ID;

        /*
         * =======================================
         * Constructors:
         * =======================================
         */

        Mode(char ID)
        {
            this.ID = ID;
        }

        /*
         * =======================================
         * Overridden methods:
         * =======================================
         */

        @Override
        public String toString()
        {
            return "" + ID;
        }

        /*
         * =======================================
         * Static methods:
         * =======================================
         */

        public static Mode getMode(char ID)
        {
            for (Mode mode : Mode.values())
                if (mode.ID == ID)
                    return mode;
            return null;
        }
    }
}
