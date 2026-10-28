package com.irignid.algos;

import com.irignid.core.SortAlgorithm;
import com.irignid.core.Tracer;
import java.util.List;

public class BubbleSortOptimised implements SortAlgorithm {
    public String name() { return "Bubble Sort (optimised)"; }

    public List<String> pseudocode() { return List.of(
        "for each pass",
        "  nobody has swapped yet",
        "  for each pair of neighbours",
        "    if the pair is out of order",
        "      swap them, note a swap",
        "  nobody swapped? stop early"); }

    public void sort(Tracer t) {
        int n = t.size();
        for (int pass = 0; pass < n - 1; pass++) {
            boolean swapped = false;
            t.flag(false, 1);
            for (int i = 0; i < n - 1 - pass; i++) {
                if (t.outOfOrder(i, i + 1, 3)) {
                    t.swap(i, i + 1, 4);
                    if (!swapped) { swapped = true; t.flag(true, 4); }
                }
            }
            t.sorted(n - 1 - pass);
            if (!swapped) {
                t.note("Nobody swapped this pass, so the line is already sorted. Stop early!", 5);
                for (int k = 0; k < n - 1 - pass; k++) t.sorted(k);   // everyone left is in place too
                return;
            }
        }
        t.sorted(0);
    }
}