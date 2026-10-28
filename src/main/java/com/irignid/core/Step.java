package com.irignid.core;

public record Step(Op op, int i, int j, int oldVal, int newVal, int codeLine, String text) {}