package ru.ifmo.ctddev.FSSARecSys.utils;

public class Pair<F, S> implements Comparable<Pair<F, S>> {

    public final F first;
    public final S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public static <F, S> Pair<F, S> of(F first, S second) {
        return new Pair<>(first, second);
    }

    public String toString() {
        return String.format("(%s, %s)", first.toString(), second.toString());
    }

    @Override
    public int compareTo(Pair<F, S> o) {
        int cr;

        if (first instanceof Comparable) {
            //noinspection unchecked
            cr = ((Comparable) first).compareTo(o.first);
        } else {
            cr = first.toString().compareTo(o.first.toString());
        }

        if (cr != 0) {
            return cr;
        }

        if (second instanceof Comparable) {
            //noinspection unchecked
            cr = ((Comparable) second).compareTo(o.second);
        } else {
            cr = second.toString().compareTo(o.second.toString());
        }

        return cr;
    }
}
