package com.codinglikeapirate.pocitaj;

import static org.junit.Assert.*;
import org.junit.Test;

public class ExerciseBookTest {

  @Test
  public void addition_Question() {
    ExerciseBook.Exercise exercise = new ExerciseBook.Addition(2, 3);
    assertEquals("2 + 3", exercise.question());
  }
}