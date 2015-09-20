package net.pslice.archebot;

import net.pslice.archebot.utilities.StringUtils;

import java.util.TreeMap;
import java.util.TreeSet;

public class User implements Comparable<User> {

    protected final TreeMap<Permission, Boolean> permissions = new TreeMap<>();
    protected final TreeSet<Character> modes = new TreeSet<>();
    protected String nick, login = "", hostmask = "", realname = "", nickservID;
    protected Server server;

    protected User() {
        this("");
    }

    protected User(String nick) {
        this.nick = nick;
        permissions.put(Permission.DEFAULT, false);
    }

    public String details() {
        return String.format("%s {LOGIN:%s} {HOSTMASK:%s} {REALNAME:%s} {SERVER:%s} {NICKSERVID:%s} {PERMISSIONS:%s} {MODES:%s}",
                nick, login, hostmask, realname, server, nickservID, StringUtils.compact(permissions.keySet()), StringUtils.compact(modes, ""));
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

    public String getNickservID() {
        return nickservID;
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

    public void givePermission(Permission permission) {
        givePermission(permission, true);
    }

    public void givePermission(Permission permission, boolean savable) {
        permissions.put(permission, savable);
    }

    public boolean hasPermission(Permission permission) {
        for (Permission p : permissions.keySet())
            if (p.includes(permission))
                return true;
        return permissions.containsKey(permission);
    }

    public boolean hasMode(char mode) {
        return modes.contains(mode);
    }

    public boolean isIdentified() {
        return nickservID != null;
    }

    public boolean isSavable(Permission permission) {
        return permissions.containsKey(permission) && permissions.get(permission);
    }

    public void removePermission(Permission permission) {
        permissions.remove(permission);
    }

    public void resetPermissions() {
        permissions.clear();
        permissions.put(Permission.DEFAULT, false);
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
