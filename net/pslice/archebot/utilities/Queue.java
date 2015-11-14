package net.pslice.archebot.utilities;

import java.util.ArrayList;

public class Queue<O> {

    private final ArrayList<O> items = new ArrayList<>();

    public Queue() {}

    public Queue(Queue<O> queue) {
        items.addAll(queue.items);
    }

    public void add(O item) {
        items.add(item);
    }

    public void clear() {
        items.clear();
    }

    public O getNext() {
        return items.remove(0);
    }

    public boolean hasNext() {
        return items.size() > 0;
    }

    public int size() {
        return items.size();
    }
}
