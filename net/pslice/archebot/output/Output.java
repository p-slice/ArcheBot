package net.pslice.archebot.output;

public class Output {

    private final String line;

    protected Output(String line) {
        this.line = line;
    }

    @Override
    public final String toString() {
        return line;
    }

    @Override
    public final boolean equals(Object obj) {
        return obj instanceof Output && obj.toString().equals(line);
    }
}
