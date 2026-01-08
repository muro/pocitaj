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
Each fact has a strength from 0 to 4. A fact is considered **mastered** when its strength reaches 5.
- **0 (Weakest):** A new or recently failed fact.
- **4 (Strongest):** A fact that is close to being mastered.

When a student answers correctly, the strength increases by 1. An incorrect answer resets the strength to 0.

### Speed Tracking (Rolling Average)
To track a user's speed for a given fact, the system stores a rolling average of their correct response times (`avgDurationMs`). This is calculated using an Exponentially Weighted Moving Average (EWMA), which naturally gives more weight to recent answers. This allows the system to adapt as the user gets faster without being unfairly penalized by old, slow answers. Incorrect answers do not affect the speed calculation.

### Question Selection (The "Working Set" Method)
To avoid boring, repetitive drills, the app uses a "Working Set" method to select questions. This ensures a student is focused on a small, manageable group of facts while still getting enough variety.

In each session, the app creates a mix of questions:
- **80% "Learning" Questions:** These come from the student's current, unmastered level.
- **20% "Review" Questions:** These come from all previously mastered levels to ensure long-term retention.

The selection of a "Learning" question follows a specific algorithm:
1.  **Identify Unmastered Facts:** The system first looks at all facts in the current level that have a strength less than 5.
2.  **Form the Working Set:**
    *   If there are **5 or fewer** unmastered facts, they all become the Working Set.
    *   If there are **more than 5** unmastered facts, the Working Set is formed from the **5 weakest facts** (based on lowest strength and oldest timestamp).
    *   If the user is starting a brand new level (i.e., there are no unmastered facts with a history), the Working Set is seeded with **5 new, random facts** from that level.
3.  **Random Selection:** The app then randomly picks one exercise from this Working Set.

This approach ensures that a student is always practicing the concepts they need most, but in a varied, engaging way. As they master a fact, it is automatically replaced with a new one from the level, creating a smooth learning progression.

## Progress Reporting: The Heatmap

Progress is visualized using a "heatmap" grid for each operation. The color of each cell shows the mastery of that specific fact.

**Example: Multiplication Heatmap**
(🟩=Mastered, 🟨=Learning, 🟥=Struggling)

| x      | 1  | 2  | 3  | 4  | 5  | 6  | 7  | 8  | 9  | 10 |
|--------|----|----|----|----|----|----|----|----|----|----|
| **1**  | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 |
| **2**  | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 |
| **3**  | 🟩 | 🟩 | 🟨 | 🟨 | 🟩 | 🟥 | 🟥 | 🟧 | 🟨 | 🟩 |
| **4**  | 🟩 | 🟩 | 🟨 | 🟨 | 🟩 | 🟧 | 🟥 | 🟧 | 🟨 | 🟩 |
| **5**  | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 |
| **6**  | 🟩 | 🟩 | 🟧 | 🟧 | 🟩 | 🟥 | 🟥 | 🟥 | 🟧 | 🟩 |
| **7**  | 🟩 | 🟩 | 🟥 | 🟥 | 🟩 | 🟥 | 🟥 | 🟥 | 🟥 | 🟩 |
| **8**  | 🟩 | 🟩 | 🟧 | 🟧 | 🟩 | 🟥 | 🟥 | 🟥 | 🟧 | 🟩 |
| **9**  | 🟩 | 🟩 | 🟨 | 🟨 | 🟩 | 🟧 | 🟥 | 🟧 | 🟨 | 🟩 |
| **10** | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 | 🟩 |

## Detailed History Tracking

For teachers and parents, the app will also maintain a complete, chronological log of every exercise attempt. This provides a detailed view of a student's practice sessions over time.

Each record in the history will include:
- The full exercise (`8 x 7`)
- Whether the answer was correct
- The time it took to answer (in milliseconds)
- The exact date and time of the attempt

This detailed log allows for a deeper analysis of a student's progress, identifying patterns in their learning speed and accuracy.
