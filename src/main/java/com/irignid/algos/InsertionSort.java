package com.irignid.algos;

import com.irignid.core.SortAlgorithm;
import com.irignid.core.Tracer;
import java.util.List;

public class InsertionSort implements SortAlgorithm {
    public String name() { return "Insertion Sort"; }

    public List<String> pseudocode() { return List.of(
        "turn everyone back except the first",
        "for each next person: turn around (key)",
        "  slide the key backward:",
        "    if left neighbour is out of order",
        "      swap with neighbour (else stop)"); }

    public void sort(Tracer t) {
        int n = t.size();
        for (int i = 1; i < n; i++) t.hide(i);        // everyone turns back except the first
        t.sorted(0);
        for (int i = 1; i < n; i++) {
            t.reveal(i, 1);                           // next person turns around
            for (int j = i; j > 0; j--) {
                if (t.outOfOrder(j - 1, j, 3)) t.swap(j - 1, j, 4);
                else break;
            }
            t.sorted(i);
        }
    }
}