package com.irignid.algos;

import com.irignid.core.SortAlgorithm;
import com.irignid.core.Tracer;
import java.util.List;

public class QuickSortOptimised implements SortAlgorithm {
    private static final int CUTOFF = 4;

    public String name() { return "Quick Sort (optimised)"; }

    public List<String> pseudocode() { return List.of(
        "quickSort(group):",
        "  small group (4 or fewer)? finish with insertion sort",
        "  pivot = median of first, middle, last (moved to the end)",
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
        if (hi - lo + 1 <= CUTOFF) { insertion(t, lo, hi); return; }
        t.range(lo, hi, 0);
        medianOfThree(t, lo, hi);
        int p = QuickSort.partition(t, lo, hi);
        t.sorted(p);
        t.clearPivot();
        quick(t, lo, p - 1);
        quick(t, p + 1, hi);
    }

    private void medianOfThree(Tracer t, int lo, int hi) {
        int mid = (lo + hi) / 2;
        t.note("Choose the pivot: the median of first, middle and last", 2);
        if (t.outOfOrder(lo, mid, 2)) t.swap(lo, mid, 2);
        if (t.outOfOrder(lo, hi, 2))  t.swap(lo, hi, 2);
        if (t.outOfOrder(mid, hi, 2)) t.swap(mid, hi, 2);
        t.swap(mid, hi, 2);                               // the median goes to the end as the pivot
    }

    private void insertion(Tracer t, int lo, int hi) {
        t.range(lo, hi, 1);
        for (int i = lo + 1; i <= hi; i++)
            for (int j = i; j > lo && t.outOfOrder(j - 1, j, 1); j--)
                t.swap(j - 1, j, 1);
        for (int k = lo; k <= hi; k++) t.sorted(k);
    }
}