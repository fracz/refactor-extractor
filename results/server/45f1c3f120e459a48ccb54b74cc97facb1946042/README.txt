commit 45f1c3f120e459a48ccb54b74cc97facb1946042
Author: Michael Göhler <somebody.here@gmx.de>
Date:   Thu Oct 11 11:38:42 2012 +0200

    further improvements on multiple login token support

    outdated tokens are deleted before checking against cookies
    if an invalid token is used we delete all stored tokens for saveness
    used token will be replaced by a new one after successful authentication