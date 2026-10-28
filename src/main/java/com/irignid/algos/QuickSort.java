package com.irignid.algos;

import com.irignid.core.SortAlgorithm;
import com.irignid.core.Tracer;
import java.util.List;

public class QuickSort implements SortAlgorithm {
    public String name() { return "Quick Sort"; }

    public List<String> pseudocode() { return List.of(
        "quickSort(group):",
        "  one person or none? already in place",
        "  pivot = last person of the group",
        "  move everyone who belongs before the pivot to the front:",
        "    compare each person with the pivot",
        "    belongs before it? swap into the front part",
        "  pivot swaps into the gap: its final place",
        "  quickSort(left of pivot), quickSort(right of pivot)"); }

    public void sort(Tracer t) {
        quick(t, 0, t.size() - 1);
        t.clearRange();
        t.note("Every pivot is in its final place: the line is sorted", 7);
    }

    private void quick(Tracer t, int lo, int hi) {
        if (lo > hi) return;
        if (lo == hi) {
            t.note("Alone in its group, so already in place", 1);
            t.sorted(lo);
            return;
        }
        t.range(lo, hi, 0);
        int p = partition(t, lo, hi);
        t.sorted(p);
        t.clearPivot();
        quick(t, lo, p - 1);
        quick(t, p + 1, hi);
    }

    /** Shared with the optimised version. Uses the last person of lo..hi as the pivot. */
    static int partition(Tracer t, int lo, int hi) {
        t.pivot(hi, 2);
        int i = lo;                                       // next spot in the front part
        for (int j = lo; j < hi; j++) {
            if (!t.outOfOrder(j, hi, 4)) {                // j belongs before the pivot (or ties)
                if (i != j) t.swap(i, j, 5);
                i++;
            }
        }
        if (i != hi) t.swap(i, hi, 6);
        return i;
    }
}