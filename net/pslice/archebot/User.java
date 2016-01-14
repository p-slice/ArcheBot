package net.pslice.archebot;

import java.util.TreeMap;
import java.util.TreeSet;

public class User implements Comparable<User> {

    protected final TreeMap<Permission, Boolean> permissions = new TreeMap<>();
    protected final TreeSet<Character> modes = new TreeSet<>();
    protected boolean known = false;
    protected String nick, login = "", hostmask = "", realname = "", nickservId;
    protected Server server;

    protected User() {
        this("");
    }

    protected User(String nick) {
        this.nick = nick;
        permissions.put(Permission.DEFAULT, false);
    }

    public void clearPermissions() {
        permissions.clear();
        permissions.put(Permission.DEFAULT, false);
    }

    public String getIdentity() {
        return nick + (login.isEmpty() ? "" : "!" + login) + (hostmask.isEmpty() ? "" : "@" + hostmask);
    }

    public String getHostmask() {
        return hostmask;
    }

    public String getLogin() {
        return login;
    }

    public TreeSet<Character> getModes() {
        return new TreeSet<>(modes);
    }

    public String getNick() {
        return nick;
    }

    public String getNickservId() {
        return nickservId;
    }

    public TreeSet<Permission> getPermissions() {
        return new TreeSet<>(permissions.keySet());
    }

    public String getRealname() {
        return realname;
    }

    public Server getServer() {
        return server;
    }

    public void givePermission(String permission) {
        givePermission(permission, true);
    }

    public void givePermission(String permission, boolean savable) {
        givePermission(Permission.get(permission), savable);
    }

    public void givePermission(Permission permission) {
        givePermission(permission, true);
    }

    public void givePermission(Permission permission, boolean savable) {
        permissions.put(permission, savable);
        for (Permission inclusion : permission.getInclusions())
            if (!hasPermission(inclusion))
                givePermission(inclusion, savable);
    }

    public boolean hasPermission(String permission) {
        return hasPermission(Permission.get(permission));
    }

    public boolean hasPermission(Permission permission) {
        return permissions.containsKey(permission);
    }

    public boolean hasMode(char mode) {
        return modes.contains(mode);
    }

    public boolean isIdentified() {
        return nickservId != null;
    }

    public boolean isKnown() {
        return known;
    }

    public boolean isSavable(String permission) {
        return isSavable(Permission.get(permission));
    }

    public boolean isSavable(Permission permission) {
        return permissions.containsKey(permission) && permissions.get(permission);
    }

    public void removePermission(String permission) {
        removePermission(Permission.get(permission));
    }

    public void removePermission(Permission permission) {
        permissions.remove(permission);
        for (Permission inclusion : permission.getInclusions())
            if (hasPermission(inclusion))
                removePermission(inclusion);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(User user) {
        return nick.compareToIgnoreCase(user.nick);
    }

    @Override
    public String toString() {
        return nick;
    }

    void addMode(char mode) {
        modes.add(mode);
    }

    void removeMode(char mode) {
        modes.remove(mode);
    }
}
