package com.codinglikeapirate.pocitaj;

import static org.junit.Assert.*;
import org.junit.Test;

public class ExerciseBookTest {

  @Test
  public void addition_Question() {
    ExerciseBook.Exercise exercise = new ExerciseBook.Addition(2, 3);
    assertEquals("2 + 3", exercise.question());
  }

  @Test
  public void addition_SolveNotRecognized() {
    ExerciseBook.Exercise exercise = new ExerciseBook.Addition(2, 3);
    assertFalse(exercise.solve(ExerciseBook.NOT_RECOGNIZED));
    assertFalse(exercise.solved());
    assertFalse(exercise.correct());
  }

  @Test
  public void addition_SolveIncorrectly() {
    ExerciseBook.Exercise exercise = new ExerciseBook.Addition(2, 3);
    assertFalse(exercise.solve(7));
    assertTrue(exercise.solved());
    assertFalse(exercise.correct());
  }

  @Test
  public void addition_SolveCorrectly() {
    ExerciseBook.Exercise exercise = new ExerciseBook.Addition(2, 3);
    assertTrue(exercise.solve(5));
    assertTrue(exercise.solved());
    assertTrue(exercise.correct());
  }
}