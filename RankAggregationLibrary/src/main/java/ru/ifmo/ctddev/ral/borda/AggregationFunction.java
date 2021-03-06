package ru.ifmo.ctddev.ral.borda;

import ru.ifmo.ctddev.ral.ListOfRanks;
import ru.ifmo.ctddev.ral.Pair;

import java.util.List;

public interface AggregationFunction<T> {
    void add(double weight, int position, T item);

    default void addAll(ListOfRanks<T> ranks) {
        for (ListOfRanks<T>.Rank rank : ranks) {
            for (T item : ranks.items()) {
                add(rank.weight, rank.R(item) + 1, item);
            }
        }
    }

    /**
     * return ordered pairs of position/item
     */
    List<Pair<Double, T>> calculate();
}
