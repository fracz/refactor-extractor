commit a57e82f5c8ab4e4a721cd5d883d57392699165e0
Author: Martin Traverso <mtraverso@gmail.com>
Date:   Mon Apr 10 13:13:41 2017 -0700

    Add InlineProjections rule

    This rule replaces the old MergeProjections optimizer and
    improves the inlining heuristic to avoid duplicating expensive
    computation.