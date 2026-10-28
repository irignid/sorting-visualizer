package com.irignid.algos;

import com.irignid.core.SortAlgorithm;
import com.irignid.core.Tracer;
import java.util.List;

public class MergeSort implements SortAlgorithm {
    public String name() { return "Merge Sort"; }

    public List<String> pseudocode() { return List.of(
        "mergeSort(group):",
        "  one person? already sorted",
        "  split in the middle, sort each half",
        "  merge: lift the group into the holding row",
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
        if (lo >= hi) return;
        int mid = (lo + hi) / 2;
        t.range(lo, hi, 2);
        split(t, lo, mid);
        split(t, mid + 1, hi);
        merge(t, lo, mid, hi);
    }

    private void merge(Tracer t, int lo, int mid, int hi) {
        t.range(lo, hi, 3);
        t.hold(lo, hi, 3);
        int a = lo, b = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (a > mid)                          t.place(k, b++, 6);
            else if (b > hi)                      t.place(k, a++, 6);
            else if (t.outOfOrderHeld(a, b, 4))   t.place(k, b++, 5);   // right head goes first
            else                                  t.place(k, a++, 5);
        }
    }
}