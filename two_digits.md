Some of the more advanced levels, like 2 digit addition with or without carry, have a lot of
associated facts. If we
were to drill all of them to level 5, it would be very repetitive and boring. Instead, we will not
create the full list
of facts, but rather dynamically generate exercise to advance.
We will split the problem into tens and ones, e.g. 23+47 is ADD_TENS_2_4 and ADD_ONES_3_7 (it's also
part of "with carry").
The strategy can no longer get a full list of facts, but rather a dynamic set of exercises. These
exercises aim to train
mastery of ADD_TENS_2_4 and similar. Every exercise increases or resets mastery of the two facts.

The drill strategy still needs to keep track of the working set, which still contains individual
facts. We do a workaround,
where we use tokens such as ADD_23_47 in the working set, which encode the two underlying facts -
ADD_TENS_2_4 and ADD_ONES_3_7.
These ephemeral tokens are created when we initialize or update the working set, based on a
selection of ADD_TENS and ADD_ONES from
the fact mastery.`
It would quickly get boring if we then only showed 23+47 to the student, so instead, we will
generate related facts whenever
this fact is chosen from the working set - we will then choose one of those related facts either
randomly or weighted random
based on mastery. The generated exercises will be used to train either the ones or the tens (based
on the exercise we chose) of the underlying facts.
Thus, when we create the exercise out of the token from the working set, we create exercises like
23+47, 25+46, 33+57, 83+17,
29+44 and similar. They all match either the ONES or TENS from the working set token ADD_23_47. We
will select an exercise
to avoid too much repetition and to progress mastery. Based on the current level, we also know
whether the it's with or
without carry.

As the student answers the exercises, we update in record two masteries in the DB, rather then the
token - the token is only
ephemeral in the working set. We update one for the tens, the other for the ones. We don't store the
ephemeral tokens for the
working set.
Every record in the DB thus updates two masteries, but only one item in history. We have a smaller
list of masteries - instead of all
combinations, we will have a shorter list for ONES and TENS.

The DrillStrategy should see minimal changes. Perhaps all that is needed is to change how we use
getNextExercise / exerciseFromFactId or their implementation.

Please check Curriculum.kt, the DrillStrategy and the database schema.

When explaining your steps, omit any praise / sycophancy and get to the point quickly. I'll ask for
more details if needed.