commit d378cab54e75244a7106948f91c3a439d123b3e2
Merge: abda671 0e3671b
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Dec 3 23:07:55 2012 +0100

    merged branch Crell/split-urlmatcher (PR #6100)

    This PR was squashed before being merged into the master branch (closes #6100).

    Commits
    -------

    0e3671b [WiP] Split urlmatcher for easier overriding

    Discussion
    ----------

    [WiP] Split urlmatcher for easier overriding

    Based on discussion in https://github.com/symfony-cmf/Routing/pull/30, this PR splits the matchCollection() method of UrlMatcher into two methods.  The reason is to allow Symfony CMF and Drupal to override just one of them, while leaving the actual meat of the class intact.

    Additionally, it switches $routes from private to protected for the same reason: It makes it possible for us to extend the class cleanly.

    Marking as WIP in case further discussion in CMF suggests other/different changes, but review and a conceptual go/no-go would be appreciated now.

    ---------------------------------------------------------------------------

    by dbu at 2012-11-25T12:57:46Z

    i think this variant really just extracts part of the logic into a separate method whithout changing any behaviour or concept. it would help a lot for the cmf to have it this way so we can extend and tweak the logic. is this now good or anybody has more input?

    ---------------------------------------------------------------------------

    by dbu at 2012-11-27T19:42:04Z

    sorry for being pushy about this one, but we need to know if this change is ok or not, if we need to improve something. if there is some problem we did not think of, we have to find different solutions for the cmf/drupal matchers.

    ---------------------------------------------------------------------------

    by fabpot at 2012-11-28T14:29:10Z

    `PhpMatcherDumper` should probably also be updated to call the new `getAttributes()` method; but that won't be possible as the Route is not accessible when using this dumper. Adding a feature that can only be used by the standard `UrlMatcher` and not by the other matchers does not sound good to me.

    ---------------------------------------------------------------------------

    by dbu at 2012-11-28T17:18:09Z

    in the context of the cmf, our problem is that we have too many routes to hold in memory. i think the dumper is not of interest for that use case - @Crell correct me please if i am wrong. if we need any caching we would need to write our own dumper probably. but currently we just extend the UrlMatcher

    ---------------------------------------------------------------------------

    by Crell at 2012-11-30T05:47:39Z

    Correct. In both the CMF case and Drupal case we have our own dumpers, because the current ones don't work for us anyway.  (1000 routes and all that. :-) )  This isn't a new public method.  It's just a small refactor of UrlMatcher itself to make it easier to extend.  If you're using a dumped PhpMatcher, I don't know why you'd be using something like NestedMatcher in the first place.