commit 21d7b63ee859d5f226d94c160852dba3d15bf530
Merge: 21d4973 76815fe
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Jul 28 08:21:01 2012 +0200

    merged branch Crell/redirect-set-target (PR #5081)

    Commits
    -------

    76815fe Allow the targetUrl on a redirect response to be set explicilty.

    Discussion
    ----------

    Allow the targetUrl on a redirect response to be set explicilty.

    Currently, RedirectResponse gets a Url set only when it's created, in the constructor.  There is no way to change it later.  That's a problem, because then you cannot change that Url from, say, a Kernel.response event listener.  That's a use case that Drupal in particular needs, because on redirects we allow modules to change the redirect target.  We also allow for redirect overrides via a GET parameter.

    This PR refactors RedirectResponse to allow for a setTargetUrl() method.  It gets called from the constructor now, and can also be called from wherever.  It does not deal with changing the status code, just the Url (and by implication the content body).

    Hopefully I got the coding style right this time... :-)

    ---------------------------------------------------------------------------

    by vicb at 2012-07-27T15:45:47Z

    > Currently, RedirectResponse gets a Url set only when it's created, in the constructor. There is no way to change it later. That's a problem, because then you cannot change that Url from, say, a Kernel.response event listener.

    You can not change the target URL, but you can create a new `RedirectResponse` to override the original one (by calling `$event->setResponse()` in the listener).