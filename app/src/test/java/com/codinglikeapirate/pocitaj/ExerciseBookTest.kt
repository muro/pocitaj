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
    assertEquals("2 + 3", exercise.question());
    assertFalse(exercise.solve(ExerciseBook.NOT_RECOGNIZED));
    assertFalse(exercise.solved());
    assertFalse(exercise.correct());
    assertEquals("2 + 3 ≠ ?", exercise.equation());
  }

  @Test
  public void addition_SolveIncorrectly() {
    ExerciseBook.Exercise exercise = new ExerciseBook.Addition(4, 2);
    assertEquals("4 + 2", exercise.question());
    assertFalse(exercise.solve(7));
    assertTrue(exercise.solved());
    assertFalse(exercise.correct());
    assertEquals("4 + 2 ≠ 7", exercise.equation());
  }

  @Test
  public void addition_SolveCorrectly() {
    ExerciseBook.Exercise exercise = new ExerciseBook.Addition(2, 3);
    assertTrue(exercise.solve(5));
    assertTrue(exercise.solved());
    assertTrue(exercise.correct());
    assertEquals("2 + 3 = 5", exercise.equation());
  }

  @Test
  public void exerciseBook_generatesQuestionAtStart() {
    ExerciseBook exerciseBook = new ExerciseBook();
    assertNotNull(exerciseBook.getLast());
  }

  @Test
  public void exerciseBook_emptyStats() {
    ExerciseBook exerciseBook = new ExerciseBook();
    assertEquals("0 / 0 (0%)", exerciseBook.getStats());
  }

  @Test
  public void exerciseBook_allWrongStats() {
    ExerciseBook exerciseBook = new ExerciseBook();
    int incorrect = 100;
    exerciseBook.getLast().solve(incorrect);
    exerciseBook.generate();
    exerciseBook.getLast().solve(incorrect);
    assertEquals("0 / 2 (0%)", exerciseBook.getStats());
  }

  @Test
  public void exerciseBook_oneSolvedOneUnsolvedWrongStats() {
    ExerciseBook exerciseBook = new ExerciseBook();
    int incorrect = 100;
    exerciseBook.getLast().solve(incorrect);
    exerciseBook.generate();
    assertEquals("0 / 1 (0%)", exerciseBook.getStats());
  }
}