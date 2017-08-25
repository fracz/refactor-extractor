commit bb93fc24aa10ae910fb4a494b0e8ef132b924758
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Thu Mar 19 21:00:03 2015 +0000

    MDL-6340 quiz: avoid reusing random questions between attempts

    There are several improvements over what we had before:

    1. We track all the questions seen in the the student's previous
    quiz attempts, so that when they start a new quiz attempt, they get
    questions they have not seen before if possible.

    2. When there are no more unseen questions, we start repeating, but
    always taking from the questions with the fewest attempts so far.

    3. A similar logic is applied with variants within one question.

    There is lots of credit to go around here. Oleg Sychev's students Alex
    Shkarupa, Sergei Bastrykin and Darya Beda all worked on this over
    several years, helping to clarify the problem and shape the best
    solution. In the end, their various attempts were rewritten into this
    final patch by me.