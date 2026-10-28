package com.irignid.algos;

import com.irignid.core.SortAlgorithm;
import java.util.List;

public final class AlgorithmRegistry {
    public static final List<SortAlgorithm> ALL = List.of(
        new BubbleSort(),
        new BubbleSortOptimised(),
        new InsertionSort(),
        new InsertionSortOptimised(),
        new SelectionSort(),
        new MergeSort(),
        new MergeSortOptimised(),
        new QuickSort(),
        new QuickSortOptimised()
    );
}