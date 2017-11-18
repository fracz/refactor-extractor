commit 738c9eaa7da209061a5d06609b05392f013373b8
Author: Ekaterina Tuzova <Ekaterina.Tuzova@jetbrains.com>
Date:   Thu Sep 15 19:19:18 2016 +0300

    OAuth stepic authorization, connector refactoring

    extracted and separated unauthorized and authorized http client for stepic so one can be confident about user login state
    removed enrolled course ids (todo: discuss with Valentina)
    added refresh/access tokens to the user information (todo: should be updated properly because it expires in 10 hours)