package com.codinglikeapirate.pocitaj;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ExerciseBook {

    public static final int NOT_RECOGNIZED = -1000;
    private final Random random = new Random(); //1234);
    private static final int BOUND = 10;

    public interface Exercise {
        // Returns the Exercise question as a string
        String question();

        // Marks the Exercise as solved and returns true if the solution is correct.
        // If the proposed solution is NOT_RECOGNIZED, doesn't set it as solved.
        boolean solve(int solution);

        // Returns true, if the Exercise has been solved.
        boolean solved();

        // Returns true if the Exercise has been correctly solved.
        boolean correct();
    }

    public static class Addition implements Exercise {
        private final int a, b;
        private int solution;
        private boolean solved = false;

        Addition(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public String question() {
            return String.format(Locale.ENGLISH, "%d + %d", a, b);
        }

        public boolean solve(int solution) {
            this.solution = solution;
            if (solution == NOT_RECOGNIZED) {
                return false;
            }
            // only set solved, if it's not the default:
            this.solved = solution != NOT_RECOGNIZED;
            return correct();
        }

        public boolean solved() {
            return solved;
        }

        public boolean correct() {
            return solved && a + b == solution;
        }

        public String equation() {
            if (correct()) {
                return String.format(Locale.ENGLISH,"%d + %d = %d", a, b, solution);
            } else {
                if (solution == NOT_RECOGNIZED) {
                    return String.format(Locale.ENGLISH, "%d + %d ≠ %s", a, b, "?");
                }
                return String.format(Locale.ENGLISH,"%d + %d ≠ %d", a, b, solution);
            }

        }
    }

    public ExerciseBook() {
        generate();
    }
    private Addition generate(int bound) {
        return new Addition(random.nextInt(bound), random.nextInt(bound));
    }

    private final List<Addition> history = new ArrayList<>();

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
        float percent = total != 0 ? 100f * correct / (float) total : 0f;
        return String.format(Locale.ENGLISH, "%d / %d (%.0f%%)", correct, total, percent);
    }
}
