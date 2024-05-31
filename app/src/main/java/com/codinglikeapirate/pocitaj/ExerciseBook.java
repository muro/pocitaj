package com.codinglikeapirate.pocitaj;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExerciseBook {

    public static final int NOTRECOGNIZED = -1000;
    private Random random = new Random(); //1234);
    private static final int BOUND = 10;

    public interface Exercise {
        public String question();
    }

    public class Addition implements Exercise {
        private final int a, b;
        private int solution;
        private boolean solved = false;

        Addition(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public String question() {
            return String.format("%d + %d", a, b);
        }

        public boolean solve(int solution) {
            this.solution = solution;
            if (solution == NOTRECOGNIZED) {
                return false;
            }
            // only set solved, if it's not the default:
            this.solved = solution != NOTRECOGNIZED;
            return correct();
        }

        public boolean isSolved() {
            return solved;
        }

        public boolean correct() {
            return solved && a + b == solution;
        }

        public String equation() {
            if (correct()) {
                return String.format("%d + %d = %d", a, b, solution);
            } else {
                if (solution == NOTRECOGNIZED) {
                    return String.format("%d + %d ≠ %s", a, b, "?");
                }
                return String.format("%d + %d ≠ %d", a, b, solution);
            }

        }
    }

    public ExerciseBook() {
        generate();
    }
    private Addition generate(int bound) {
        return new Addition(random.nextInt(bound), random.nextInt(bound));
    }

    private List<Addition> history = new ArrayList<>();

    public void generate() {
        history.add(generate(BOUND));
    }

    public Addition getLast() {
        return history.get(history.size() - 1);
    }

    public String getStats() {
        int total = history.size();
        int correct = 0;
        for (Addition a : history) {
            if (a.correct()) {
                correct++;
            }
        }
        float percent = total != 0 ? (float) 100f * correct / (float) total : 0f;
        return String.format("%d / %d (%.0f%%)", correct, total, percent);
    }
}
