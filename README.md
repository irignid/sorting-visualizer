# Sorting Visualizer

A JavaFX desktop app that shows sorting algorithms as a line of people. Every comparison, swap and move is animated step by step, with live counters and a highlighted pseudocode panel, so you can see *why* an algorithm does what it does.

![Quick Sort (optimised) finishing on 10 people](docs/screenshot.png)

## Features

- **8 algorithms**, each with its optimised variant where one exists (see the table below)
- **Order and direction:** ascending or descending, starting from the left or the right
- **Full playback control:** play/pause, step forward and back, reset, a speed slider and a scrubber to jump to any step
- **Pseudocode panel** that highlights the line currently executing (can be shown or hidden)
- **Live counters** for comparisons and writes, so you can compare an algorithm with its optimised version on the same input
- **Input presets:** random, nearly sorted, reversed and few unique values, with 5 to 30 people
- **Drag to reorder** the line before you start, and **double-click** a person to rename them
- **Dark mode**
- Each person carries a name and a coloured cap, so equal heights stay distinguishable and you can see which algorithms are stable

## Algorithms

| Algorithm | Best | Average | Worst | Stable |
|---|---|---|---|---|
| Bubble Sort | O(n²) | O(n²) | O(n²) | Yes |
| Bubble Sort (optimised) | O(n) | O(n²) | O(n²) | Yes |
| Insertion Sort | O(n) | O(n²) | O(n²) | Yes |
| Insertion Sort (optimised) | O(n) | O(n²) | O(n²) | Yes |
| Selection Sort | O(n²) | O(n²) | O(n²) | No |
| Merge Sort | O(n log n) | O(n log n) | O(n log n) | Yes |
| Quick Sort | O(n log n) | O(n log n) | O(n²) | No |
| Quick Sort (optimised) | O(n log n) | O(n log n) | O(n²) | No |

What the optimised variants change:

- **Bubble:** a `swapped` flag stops the sort after a pass with no swaps.
- **Insertion:** people already in place never leave the line, and when someone does move, neighbours slide over instead of swapping, which means fewer writes.
- **Quick:** picks the pivot as the median of the first, middle and last person, and finishes small groups with insertion sort.

These are standard textbook improvements to each algorithm rather than separate named algorithms.

## Running it

**Requirements:** JDK 21 or newer and Maven 3.8 or newer.

```bash
git clone https://github.com/irignid/sorting-visualizer.git
cd sorting-visualizer
mvn clean javafx:run
```

JavaFX is downloaded by Maven, so there's nothing else to install.

If you run it from an IDE, launch it through the Maven `javafx:run` goal. Running `App.main` directly can fail with "JavaFX runtime components are missing".

## How it works

The algorithms never draw anything. Instead, each one runs instantly against a `Tracer`, which performs every operation and records it as a `Step`. A `Player` then replays those steps for the UI.

```
algorithm.sort(tracer)  ->  List<Step>  ->  Player  ->  animated view
```

Because the steps are recorded up front, stepping backwards, scrubbing and speed control work for every algorithm without any per-algorithm code. The step vocabulary covers compare, swap, mark sorted, hide/reveal (insertion's turned-around people), marker (selection), lift/shift/drop (optimised insertion), range, hold/place (merge), and pivot (quick).

```
src/main/java/com/irignid/
  core/    Op, Step, Tracer, SortAlgorithm, Order, Direction
  algos/   one class per algorithm, plus AlgorithmRegistry
  ui/      Player (replays steps into a drawable state)
  App.java the JavaFX window, canvas rendering and controls
```

## Adding a new algorithm

1. Create a class in `algos/` implementing `SortAlgorithm`. Write the sort in textbook style, using only `Tracer` calls (`outOfOrder`, `swap`, `sorted`, and so on) and passing the pseudocode line number for each one.
2. Add one line to `AlgorithmRegistry`.

```java
public class MySort implements SortAlgorithm {
    public String name() { return "My Sort"; }

    public List<String> pseudocode() { return List.of(
        "for each pair of neighbours",
        "  if the pair is out of order",
        "    swap them"); }

    public void sort(Tracer t) {
        for (int i = 0; i < t.size() - 1; i++) {
            if (t.outOfOrder(i, i + 1, 1)) t.swap(i, i + 1, 2);
        }
    }
}
```

The dropdown, counters, pseudocode highlighting, order/direction handling and playback controls then work with no further changes. If your algorithm needs something the existing steps can't express, add a new `Op` and handle it in `Player` and the renderer.

## Built with

Java, JavaFX and Maven.

## Author

irignid

## License

CC0
