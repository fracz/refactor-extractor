commit 31cd0ff07d5a317eb8f3f729b97d168fe507e1bc
Author: Mark Story <mark@mark-story.com>
Date:   Wed Oct 29 21:08:04 2014 -0400

    Use chdir() instead of calling multiple commands.

    This improves compatibility with windows, and continues to work on *nix
    systems.

    Refs #5011