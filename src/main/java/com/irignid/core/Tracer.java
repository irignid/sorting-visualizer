package com.irignid.core;

import java.util.ArrayList;
import java.util.List;

public class Tracer {
    private final int[] data;
    private final List<Step> steps = new ArrayList<>();
    private int comparisons, writes, auxLoP ;
    private int[] aux = new int[0];

    private final Order order;
    private final Direction dir;
    private int marker = -1;

    public Tracer(int[] input, Order order, Direction dir) {
        this.data = input.clone();
        this.order = order;
        this.dir = dir;
    }

    private int p(int i) { return dir == Direction.RIGHT ? data.length - 1 - i : i; }

    public boolean outOfOrder(int i, int j, int line) {
        comparisons++;
        int a = p(i), b = p(j);
        steps.add(new Step(Op.COMPARE, a, b, 0, 0, line,
            "Compare " + data[a] + " and " + data[b]));
        int left = Math.min(a, b), right = Math.max(a, b);
        return order == Order.ASC ? data[left] > data[right] : data[left] < data[right];
    }

    public void swap(int i, int j, int line) {
        int a = p(i), b = p(j);
        writes += 2;
        steps.add(new Step(Op.SWAP, a, b, 0, 0, line,
            "Swap " + data[a] + " and " + data[b]));
        int t = data[a]; data[a] = data[b]; data[b] = t;
    }

    public void set(int i, int value, int line) {
        int a = p(i);
        writes++;
        steps.add(new Step(Op.SET, a, -1, data[a], value, line,
            "Place " + value + " at position " + a));
        data[a] = value;
    }

    public void hide(int i)   { steps.add(new Step(Op.HIDE,   p(i), -1, 0, 0, -1, "")); }
    
    public void reveal(int i, int line) {
        steps.add(new Step(Op.REVEAL, p(i), -1, 0, 0, line,
            "Turn " + data[p(i)] + " around: this is the key"));
    }
    
    public void marker(int i, int line) {
        int old = marker;
        marker = p(i);
        steps.add(new Step(Op.MARKER, marker, -1, old, 0, line, data[marker] + " holds the marker"));
    }

    public void flag(boolean value, int line) {
        steps.add(new Step(Op.FLAG, -1, -1, 0, value ? 1 : 0, line,
            value ? "Someone swapped: flag is now TRUE" : "New pass: flag reset to FALSE"));
    }

    public void lift(int i, int line) {
        int a = p(i);
        writes++;
        steps.add(new Step(Op.LIFT, a, -1, 0, 0, line,
            "Lift " + data[a] + " out of the line: this is the key"));
    }

    public void shift(int from, int to, int line) {      // person at 'from' slides to 'to'
        int a = p(from), b = p(to);
        writes++;
        steps.add(new Step(Op.SHIFT, a, b, 0, 0, line, data[a] + " steps aside to make room"));
        int t = data[a]; data[a] = data[b]; data[b] = t;  // key's logical place moves one over
    }

    public void drop(int i, int line) {
        int a = p(i);
        writes++;
        steps.add(new Step(Op.DROP, a, -1, 0, 0, line, "Drop " + data[a] + " into the gap"));
    }

    public void range(int lo, int hi, int line) {
        int a = p(lo), b = p(hi);
        steps.add(new Step(Op.RANGE, Math.min(a, b), Math.max(a, b), 0, 0, line,
            "Working on positions " + (Math.min(a, b) + 1) + " to " + (Math.max(a, b) + 1)));
    }

    public void clearRange() { steps.add(new Step(Op.RANGE, -1, -1, 0, 0, -1, "")); }

    public void hold(int lo, int hi, int line) {
        int a = p(lo), b = p(hi);
        auxLoP = Math.min(a, b);
        aux = java.util.Arrays.copyOfRange(data, auxLoP, Math.max(a, b) + 1);
        steps.add(new Step(Op.HOLD, auxLoP, auxLoP + aux.length - 1, 0, 0, line,
            "Lift this group into the holding row to merge it"));
    }

    public boolean outOfOrderHeld(int i, int j, int line) {   // compares two held people
        comparisons++;
        int a = p(i), b = p(j);
        steps.add(new Step(Op.COMPARE_AUX, a, b, 0, 0, line,
            "Compare " + aux[a - auxLoP] + " and " + aux[b - auxLoP]));
        int lv = aux[Math.min(a, b) - auxLoP], rv = aux[Math.max(a, b) - auxLoP];
        return order == Order.ASC ? lv > rv : lv < rv;
    }

    public void place(int k, int src, int line) {             // held person 'src' drops into gap 'k'
        int a = p(k), s = p(src);
        writes++;
        steps.add(new Step(Op.PLACE, a, s, 0, 0, line,
            "Place " + aux[s - auxLoP] + " into position " + (a + 1)));
        data[a] = aux[s - auxLoP];
    }

    public void pivot(int i, int line) {
        int a = p(i);
        steps.add(new Step(Op.PIVOT, a, -1, 0, 0, line,
            "Pivot: " + data[a] + " is who everyone is measured against"));
    }

    public void clearPivot() { steps.add(new Step(Op.PIVOT, -1, -1, 0, 0, -1, "")); }

    public void sorted(int i) { steps.add(new Step(Op.MARK_SORTED, p(i), -1, 0, 0, -1, "")); }
    public void note(String msg, int line) { steps.add(new Step(Op.NOTE, -1, -1, 0, 0, line, msg)); }

    public int get(int i) { return data[p(i)]; }
    public int size() { return data.length; }
    public int comparisons() { return comparisons; }
    public int writes() { return writes; }
    public List<Step> steps() { return steps; }
}