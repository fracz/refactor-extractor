commit 1547c751aa48efe7dbefef701c3df5983b04aa2e
Author: Peter Bacon Darwin <pete@bacondarwin.com>
Date:   Tue Sep 6 14:33:23 2016 +0100

    refactor($parse): remove Angular expression sandbox

    The angular expression parser (`$parse`) attempts to sandbox expressions
    to prevent unrestricted access to the global context.

    While the sandbox was not on the frontline of the security defense,
    developers kept relying upon it as a security feature even though it was
    always possible to access arbitrary JavaScript code if a malicious user
    could control the content of Angular templates in applications.

    This commit removes this sandbox, which has the following benefits:

    * it sends a clear message to developers that they should not rely on
    the sandbox to prevent XSS attacks; that they must prevent control of
    expression and templates instead.
    * it allows performance and size improvements in the core Angular 1
    library.
    * it simplifies maintenance and provides opportunities to make the
    parser more capable.

    Please see the [Sandbox Removal Blog Post](http://angularjs.blogspot.com/2016/09/angular-16-expression-sandbox-removal.html)
    for more detail on what you should do to ensure that your application is
    secure.

    Closes #15094