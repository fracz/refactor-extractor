commit d869de66a6bf6729ae242a662732fea6acf9ba0d
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Mon Aug 13 13:48:22 2012 +0100

    MDL-34733 quiz 'secure' mode: finish review link broken in previews

    This fix a small API change in mod_quiz_renderer::finish_review_link.
    At least the required change is an improvement, since it gives the
    renderer more flexibility.