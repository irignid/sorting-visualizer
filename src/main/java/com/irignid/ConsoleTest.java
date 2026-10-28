package com.irignid;

import com.irignid.algos.BubbleSort;
// import com.irignid.algos.InsertionSort;
// import com.irignid.algos.SelectionSort;
import com.irignid.core.Direction;
import com.irignid.core.Order;
import com.irignid.core.Tracer;

public class ConsoleTest {
    public static void main(String[] args) {
        //Tracer t = new Tracer(new int[]{150, 142, 170, 135, 160}, Order.ASC, Direction.RIGHT);
        Tracer t = new Tracer(new int[]{150, 142, 170, 135, 160}, Order.DESC, Direction.LEFT);
        new BubbleSort().sort(t);
        t.steps().forEach(s -> System.out.println(s.op() + " " + s.i() + "," + s.j() + "  " + s.text()));
        System.out.println("Comparisons: " + t.comparisons() + ", writes: " + t.writes());
    }
}