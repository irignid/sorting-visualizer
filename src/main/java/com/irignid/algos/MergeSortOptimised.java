package com.irignid.algos;

import com.irignid.core.SortAlgorithm;
import com.irignid.core.Tracer;
import java.util.List;

public class MergeSortOptimised implements SortAlgorithm {
    private static final int CUTOFF = 4;   // groups this small are finished with insertion sort

    public String name() { return "Merge Sort (optimised)"; }

    public List<String> pseudocode() { return List.of(
        "mergeSort(group):",
        "  small group? finish it with insertion sort",
        "  split in the middle, sort each half",
        "  already in order (left's last, right's first)? skip the merge",
        "  otherwise lift the group into the holding row",
        "    compare the first person of each half",
        "    the one who goes first drops into the next gap",
        "    one half empty? drop the rest of the other"); }

    public void sort(Tracer t) {
        int n = t.size();
        split(t, 0, n - 1);
        t.clearRange();
        for (int i = 0; i < n; i++) t.sorted(i);
        t.note("Every group has been merged: the line is sorted", 0);
    }

    private void split(Tracer t, int lo, int hi) {
        if (hi - lo + 1 <= CUTOFF) {
            if (hi > lo) insertion(t, lo, hi);
            return;
        }
        int mid = (lo + hi) / 2;
        t.range(lo, hi, 2);
        split(t, lo, mid);
        split(t, mid + 1, hi);
        t.range(lo, hi, 3);
        if (!t.outOfOrder(mid, mid + 1, 3)) {
            t.note("Already in order, so skip the merge!", 3);
            return;
        }
        merge(t, lo, mid, hi);
    }

    private void insertion(Tracer t, int lo, int hi) {
        t.range(lo, hi, 1);
        for (int i = lo + 1; i <= hi; i++)
            for (int j = i; j > lo && t.outOfOrder(j - 1, j, 1); j--)
                t.swap(j - 1, j, 1);
    }

    private void merge(Tracer t, int lo, int mid, int hi) {
        t.hold(lo, hi, 4);
        int a = lo, b = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (a > mid)                          t.place(k, b++, 7);
            else if (b > hi)                      t.place(k, a++, 7);
            else if (t.outOfOrderHeld(a, b, 5))   t.place(k, b++, 6);   // right head goes first
            else                                  t.place(k, a++, 6);
        }
    }
}