package com.irignid.algos;

import com.irignid.core.SortAlgorithm;
import com.irignid.core.Tracer;
import java.util.List;

public class SelectionSort implements SortAlgorithm {
    public String name() { return "Selection Sort"; }

    public List<String> pseudocode() { return List.of(
        "for each start position",
        "  marker = start person",
        "  for each person after start",
        "    if marker person is out of order with them",
        "      pass the marker to them",
        "  swap start person with marker person"); }

    public void sort(Tracer t) {
        int n = t.size();
        for (int start = 0; start < n - 1; start++) {
            int m = start;
            t.marker(m, 1);                            // first person holds the object
            for (int j = start + 1; j < n; j++) {
                if (t.outOfOrder(m, j, 3)) { m = j; t.marker(m, 4); }  // pass it on
            }
            if (m != start) t.swap(start, m, 5);       // round ends: swap
            t.sorted(start);
        }
        t.sorted(n - 1);
    }
}
