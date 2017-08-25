commit 441d284ada20943b556aaa8d73a6cc3909f04f91
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Thu Feb 26 11:53:00 2015 +0000

    MDL-40990 quiz: option to require prev Q finished before next shown

    Further improvements to this code, including resolving edge cases:

    * The new feature can only be used when it is possible for the
    previous question in the quiz to be complete.

    * Also, this new feature cannot be used in combination with shuffle
    questions, because that make no sense; nor in combination with
    sequential navigation, because to make that work properly would be a lot
    of effort. If someone needs that to work later, it should be possible
    for them to implement it.

    * There were changes in the edit renderer API, to try to make things
    more  consistent, and to make it less likely we will need to change
    things again in the future. See mod/quiz/upgrade.txt.

    * As part of this change, the styling of the Edit quiz page was tweaked
    to make slighly more efficient use of the horizontal space, and to be
    more symmetrical.