package net.pslice.archebot;

public class Mode implements Comparable<Mode> {

    private final char ID, prefix;
    private final Type type;

    Mode(char ID, Type type) {
        this(ID, type, ID);
    }

    Mode(char ID, Type type, char prefix) {
        this.ID = ID;
        this.type = type;
        this.prefix = prefix;
    }

    public char getID() {
        return ID;
    }

    public char getPrefix() {
        return prefix;
    }

    public Type getType() {
        return type;
    }

    public boolean isList() {
        return type == Type.LIST;
    }

    public boolean isStatus() {
        return type == Type.STATUS;
    }

    public boolean isUser() {
        return type == Type.USER;
    }

    public boolean isValue() {
        return type == Type.VALUE;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Mode mode) {
        return toString().compareTo(mode.toString());
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Mode) && (type == ((Mode) o).type && ID == ((Mode) o).ID);
    }

    @Override
    public String toString() {
        return "" + ID;
    }

    public enum Type {
        STATUS, VALUE, LIST, USER
    }
}
