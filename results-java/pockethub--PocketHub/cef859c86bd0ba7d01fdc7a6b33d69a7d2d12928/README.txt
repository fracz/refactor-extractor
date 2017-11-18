commit cef859c86bd0ba7d01fdc7a6b33d69a7d2d12928
Author: Roberto Tyley <roberto@github.com>
Date:   Thu Apr 5 16:16:59 2012 +0100

    Fix initiation/ensuring of user-login

    The problem with making sure the user is authenticated is that getting
    them to sign in is a process that will block, so you can't reasonably
    expect anything that occurs on the main thread to have a guaranteed
    reference to authenticated GitHub credentials.

    Fortunately, when people are doing network access it *has* to be on a
    background thread. We can enforce that these background threads have
    required the user to login, so at the point of executing the api
    request, we will have credentials and can set them on the request.

    Implementation-wise the important classes for this change are
    AuthenticatedUserLoader & AuthenticatedUserTask, which enforce
    that the user must be signed in before the rest of their background
    threads run, LateAuthenticatedGitHubClient which only attempts to read
    the GitHub credentials immediately before making it's API request
    (which is implicitly on the background thread) and GitHubAccountScope,
    a custom Guice scope used by the async loader/task classes to ensure
    that the user is logged in and to make those GitHubAccount credentials
    available for injection.

    This commit also improves the background android-account sync in that
    it now respects the account supplied to onPerformSync().