package com.irignid.algos;

import com.irignid.core.SortAlgorithm;
import com.irignid.core.Tracer;
import java.util.List;

public class BubbleSort implements SortAlgorithm {
    public String name() { return "Bubble Sort"; }

    public List<String> pseudocode() { return List.of(
        "for each pass",
        "  for each pair of neighbours",
        "    if the pair is out of order",
        "      swap them"); }

    public void sort(Tracer t) {
        int n = t.size();
        for (int pass = 0; pass < n - 1; pass++) {
            for (int i = 0; i < n - 1 - pass; i++) {
                if (t.outOfOrder(i, i + 1, 2)) t.swap(i, i + 1, 3);
            }
            t.sorted(n - 1 - pass);
        }
        t.sorted(0);
    }
}