# Pocitaj Learning Model

This document outlines the adaptive learning system used in the Pocitaj app to help students master arithmetic. The system is designed to be automatic, requiring no configuration from the user, and is based on the principles of Spaced Repetition and Mastery Learning.

## Core Concepts

The system is built on two main ideas:
1.  **Mastery Levels:** Learning is broken down into a structured curriculum of levels, from simple concepts to more complex ones. A student must master one level before moving to the next.
2.  **Fact Strength (Spaced Repetition):** Every individual arithmetic fact (e.g., `7x8`) has a "strength" score. The system shows facts the student struggles with more frequently, and facts they know well less frequently.

## The Curriculum

The app follows a structured curriculum for each of the four main arithmetic operations.

### Addition
- **Level 1:** Sums up to 5
- **Level 2:** Sums up to 10
- **Level 3:** Sums up to 20 (introduces "carrying over")
- **Level 4:** Adding Tens
- **Level 5:** Two-Digit Addition (No Carry)
- **Level 6:** Two-Digit Addition (With Carry)

### Subtraction
- **Level 1:** From numbers up to 5
- **Level 2:** From numbers up to 10
- **Level 3:** From numbers up to 20 (introduces "borrowing")
- **Level 4:** Subtracting Tens
- **Level 5:** Two-Digit Subtraction (No Borrow)
- **Level 6:** Two-Digit Subtraction (With Borrow)

### Multiplication
- **Level 1:** Tables: 0, 1, 2, 5, 10
- **Level 2:** Tables: 3, 4, 6
- **Level 3:** Tables: 7, 8, 9
- **Level 4:** Full 10x10 Grid Review

### Division
- **Level 1:** Dividing by 2, 5, 10
- **Level 2:** Dividing by 3, 4, 6
- **Level 3:** Dividing by 7, 8, 9
- **Level 4:** Full 10x10 Grid Review

## The Learning Algorithm

### Fact Strength
Each fact has a strength from 1 to 5.
- **1 (Weakest):** A new or recently failed fact.
- **5 (Strongest):** A mastered fact.

When a student answers correctly, the strength increases by 1. An incorrect answer resets the strength to 1.

### Question Selection
In each session, the app creates a mix of questions:
- **80% "New" Questions:** From the student's current, unmastered level. The app prioritizes facts with the lowest strength.
- **20% "Review" Questions:** From all previously mastered levels to ensure long-term retention.

## Progress Reporting: The Heatmap

Progress is visualized using a "heatmap" grid for each operation. The color of each cell shows the mastery of that specific fact.

**Example: Multiplication Heatmap**
(🟩=Mastered, 🟨=Learning, 🟥=Struggling)

| x | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
|---|---|---|---|---|---|---|---|---|---|---|
| **1** |🟩|🟩|🟩|🟩|🟩|🟩|🟩|🟩|🟩|🟩|
| **2** |🟩|🟩|🟩|🟩|🟩|🟩|🟩|🟩|🟩|🟩|
| **3** |🟩|🟩|🟨|🟨|🟩|🟥|🟥|🟧|🟨|🟩|
| **4** |🟩|🟩|🟨|🟨|🟩|🟧|🟥|🟧|🟨|🟩|
| **5** |🟩|🟩|🟩|🟩|🟩|🟩|🟩|🟩|🟩|🟩|
| **6** |🟩|🟩|🟧|🟧|🟩|🟥|🟥|🟥|🟧|🟩|
| **7** |🟩|🟩|🟥|🟥|🟩|🟥|🟥|🟥|🟥|🟩|
| **8** |🟩|🟩|🟧|🟧|🟩|🟥|🟥|🟥|🟧|🟩|
| **9** |🟩|🟩|🟨|🟨|🟩|🟧|🟥|🟧|🟨|🟩|
| **10**|🟩|🟩|🟩|🟩|🟩|🟩|🟩|🟩|🟩|🟩|

## Detailed History Tracking

For teachers and parents, the app will also maintain a complete, chronological log of every exercise attempt. This provides a detailed view of a student's practice sessions over time.

Each record in the history will include:
- The full exercise (`8 x 7`)
- Whether the answer was correct
- The time it took to answer (in milliseconds)
- The exact date and time of the attempt

This detailed log allows for a deeper analysis of a student's progress, identifying patterns in their learning speed and accuracy.
