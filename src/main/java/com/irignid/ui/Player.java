package com.irignid.ui;

import com.irignid.core.Step;
import java.util.List;

public class Player {

    /** Everything the renderer needs to draw one moment in time. */
    public static class View {
        public int[] values, ids;
        public boolean[] hidden, sorted;
        public int pivot = -1, rangeLo = -1, rangeHi = -1, auxLo, auxCmpA = -1, auxCmpB = -1, flag = -1, lifted = -1, codeLine = -1, marker = -1, cmpA = -1, cmpB = -1, swapA = -1, swapB = -1, reveal = -1;
        public int[] auxIds = new int[0], auxVals = new int[0];
        public boolean[] auxUsed = new boolean[0];
        public int comparisons, writes;
        public String text = "";
    }

    private final int[] initial;
    private final List<Step> steps;
    private int index;
    private View view;

    public Player(int[] initial, List<Step> steps) {
        this.initial = initial.clone();
        this.steps = steps;
        goTo(0);
    }

    public View view() { return view; }
    public int index() { return index; }
    public int total() { return steps.size(); }
    public boolean atEnd() { return index >= steps.size(); }

    public void next() {
        if (atEnd()) return;
        apply(view, steps.get(index));
        index++;
    }

    public void prev() { if (index > 0) goTo(index - 1); }

    public void goTo(int target) {
        view = fresh();
        index = 0;
        while (index < target && index < steps.size()) next();
    }

    private View fresh() {
        View v = new View();
        v.values = initial.clone();
        v.hidden = new boolean[initial.length];
        v.sorted = new boolean[initial.length];
        v.ids = new int[initial.length];
        for (int i = 0; i < initial.length; i++) v.ids[i] = i;
        return v;
    }

    private void apply(View v, Step s) {
        v.cmpA = v.cmpB = v.swapA = v.swapB = v.reveal = v.auxCmpA = v.auxCmpB = -1;   // per-step highlights
        if (!s.text().isEmpty()) v.text = s.text();
        if (s.codeLine() >= 0) v.codeLine = s.codeLine();
        switch (s.op()) {
            case COMPARE -> { v.cmpA = s.i(); v.cmpB = s.j(); v.comparisons++; }
            case SWAP -> {
                int t = v.values[s.i()]; v.values[s.i()] = v.values[s.j()]; v.values[s.j()] = t;
                v.swapA = s.i(); v.swapB = s.j(); v.writes += 2;
                int u = v.ids[s.i()]; v.ids[s.i()] = v.ids[s.j()]; v.ids[s.j()] = u;
                if (v.pivot == s.i()) v.pivot = s.j(); else if (v.pivot == s.j()) v.pivot = s.i();
            }
            case SET -> { v.values[s.i()] = s.newVal(); v.writes++; }
            case MARK_SORTED -> v.sorted[s.i()] = true;
            case HIDE -> v.hidden[s.i()] = true;
            case REVEAL -> { v.hidden[s.i()] = false; v.reveal = s.i(); }
            case MARKER -> v.marker = s.i();
            case NOTE -> { }
            case FLAG -> v.flag = s.newVal();
            case LIFT -> { v.lifted = s.i(); v.hidden[s.i()] = false; v.writes++; }
            case SHIFT -> {
                int t = v.values[s.i()]; v.values[s.i()] = v.values[s.j()]; v.values[s.j()] = t;
                int u = v.ids[s.i()]; v.ids[s.i()] = v.ids[s.j()]; v.ids[s.j()] = u;
                if (v.lifted == s.j()) v.lifted = s.i();
                v.swapA = s.i(); v.swapB = s.j(); v.writes++; }
            case DROP -> { v.lifted = -1; v.writes++; }
            case RANGE -> { v.rangeLo = s.i(); v.rangeHi = s.j(); }
            case HOLD -> {
                int len = s.j() - s.i() + 1;
                v.auxLo = s.i();
                v.auxIds = new int[len]; v.auxVals = new int[len]; v.auxUsed = new boolean[len];
                for (int k = 0; k < len; k++) {
                    v.auxIds[k] = v.ids[s.i() + k];
                    v.auxVals[k] = v.values[s.i() + k];
                    v.ids[s.i() + k] = -1;                         // the gap
                }
            }
            case COMPARE_AUX -> { v.auxCmpA = s.i() - v.auxLo; v.auxCmpB = s.j() - v.auxLo; v.comparisons++; }
            case PLACE -> {
                int src = s.j() - v.auxLo;
                v.values[s.i()] = v.auxVals[src];
                v.ids[s.i()] = v.auxIds[src];
                v.auxUsed[src] = true;
                v.writes++;
            }
            case PIVOT -> v.pivot = s.i();
        }
    }
}