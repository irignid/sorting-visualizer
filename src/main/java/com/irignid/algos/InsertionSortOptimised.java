package com.irignid.algos;

import com.irignid.core.SortAlgorithm;
import com.irignid.core.Tracer;
import java.util.List;

public class InsertionSortOptimised implements SortAlgorithm {
    public String name() { return "Insertion Sort (optimised)"; }

    public List<String> pseudocode() { return List.of(
        "turn everyone back except the first",
        "for each next person: step out of the line (key)",
        "  while the left neighbour is out of order with the key",
        "    neighbour slides over one place",
        "  key steps into the gap"); }

    public void sort(Tracer t) {
        int n = t.size();
        for (int i = 1; i < n; i++) t.hide(i);
        t.sorted(0);
        for (int i = 1; i < n; i++) {
            t.lift(i, 1);
            int j = i;
            while (j > 0 && t.outOfOrder(j - 1, j, 2)) {
                t.shift(j - 1, j, 3);
                j--;
            }
            t.drop(j, 4);
            t.sorted(i);
        }
    }
}