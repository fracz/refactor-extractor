commit 866b13e5fc2cd906126847f4e527783a0541eee7
Merge: b71d42a 1c290d7
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Apr 20 07:41:36 2012 +0200

    merged branch Crell/flatten-exception (PR #4017)

    Commits
    -------

    1c290d7 Add unit tests for FlattenException::getLine() and FlattenException::getFile().
    a22f0cd Enhance FlattenException to include more methods from Exception.  That allows it to be used in place of Exception in more places.

    Discussion
    ----------

    [HttpKernel] Enhance FlattenException to include more methods from Exception.

    I'm trying to retrofit FlattenException into Drupal, in places where Drupal expects an Exception.  That doesn't quite work though, as FlattenException only has some of the methods from Exception.  I'm not entirely clear why it only has some, but this PR adds getFile() and getLine() so that it's a more ready drop-in.  I did not add them to the toArray() method for fear of breaking BC somewhere, but that could be done as well no doubt if folks felt it was appropriate.

    Note: While the parts of Drupal in question will get rewritten later anyway, I think having this information exposed is a good thing in general for logging purposes if nothing else.  It's already possible to dig it out of the trace, so this is just an improved "Developer eXperience" (DX).

    ---------------------------------------------------------------------------

    by fabpot at 2012-04-20T04:34:54Z

    I'm +1 to make `FlattenException` more "compatible" with `Exception`. Can you add the other missing methods? Also, you need to populate the `$this->file` and `$this->line` value in the constructor.

    ---------------------------------------------------------------------------

    by Crell at 2012-04-20T04:48:40Z

    I knew I was forgetting something obvious...

    According to http://us.php.net/manual/en/class.exception.php, I think the only other missing method is http://us.php.net/manual/en/exception.gettraceasstring.php.  I'm not sure how useful that is, but I can try to approximate it if you think it's necessary.  (Honestly I've never used that method on an exception myself.)

    I should probably add some tests, too.  Stand by for those.

    ---------------------------------------------------------------------------

    by Crell at 2012-04-20T05:00:28Z

    Now includes unit tests to make sure I didn't do anything stupid this time.  I'll hold off on getTraceAsString() for now unless you think it's needed.  (I'm not sure it is since it's harder to do and IMO less useful.)