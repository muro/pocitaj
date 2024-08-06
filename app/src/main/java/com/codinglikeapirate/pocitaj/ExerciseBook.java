package com.codinglikeapirate.pocitaj;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ExerciseBook {

  public static final int NOT_RECOGNIZED = -1000;
  private static final int BOUND = 10;
  private final Random random = new Random(); //1234);
  private final List<Addition> history = new ArrayList<>();

  public ExerciseBook() {
    generate();
  }

  private Addition generate(int bound) {
    return new Addition(random.nextInt(bound), random.nextInt(bound));
  }

  public void generate() {
    history.add(generate(BOUND));
  }

  public Addition getLast() {
    return history.get(history.size() - 1);
  }

  public String getStats() {
    int solved = 0;
    int correct = 0;
    for (Addition a : history) {
      if (a.solved()) {
        solved++;
      }
      if (a.correct()) {
        correct++;
      }
    }
    float percent = solved != 0 ? 100f * correct / (float) solved : 0f;
    return String.format(Locale.ENGLISH, "%d / %d (%.0f%%)", correct, solved, percent);
  }

  public List<Exercise> getHistory() {
    List<Exercise> hist = new ArrayList<>();
    hist.addAll(this.history);
    return hist;
  }

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

    // Returns the full equation as a string.
    String equation();
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
      this.solved = true;
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
        return String.format(Locale.ENGLISH, "%d + %d = %d", a, b, solution);
      } else {
        if (solution == NOT_RECOGNIZED) {
          return String.format(Locale.ENGLISH, "%d + %d ≠ ?", a, b);
        }
        return String.format(Locale.ENGLISH, "%d + %d ≠ %d", a, b, solution);
      }

    }
  }
}
