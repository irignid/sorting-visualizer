package com.irignid.core;

import java.util.List;

public interface SortAlgorithm {
    String name();
    List<String> pseudocode();
    void sort(Tracer t);
}