commit c321297a3d891f6769c0fa5391f65a9b80a617e5
Author: SteveJobzniak <SteveJobzniak@users.noreply.github.com>
Date:   Thu May 25 23:23:16 2017 +0200

    Massive refactoring of token cookie handling

    This rewrites lots of strange design decisions which were ported over
    from old v1.x code due to lack of time. The v2.x release rewrite was
    done in many phases, starting from very messy v1.x code, and these
    particular fixes were things which had to wait until the rest of the
    codebase of 2.x was finalized, before they could be rewritten too.

    - We no longer store "token" in the storage backend. Instead, it's only
      stored within the user's cookie jar. That's more logical, since the
      token is literally a cookie, so now we treat it as such.

    - "Request::getResponse()" no longer requires a base class. If no class
      is provided, you get the raw response object as a standard PHP object.
      This is useful for people who are writing their own API call
      extensions which aren't part of this library. You no longer have to
      create a baseclass for your responses while testing.

    - "Request::getResponse()" can no longer be told to "include header" to
      include the token in an ugly "getFullResponse()" array in the return
      value, since we no longer need to extract the token that way.

    - All places that used "$this->token" now use
      "$this->client->getToken()" instead, which reads the token directly
      from the current user's cookie jar.

    - Login and similar things are now simpler, since they just need to call
      specific endpoints that place a csrftoken in the cookie jar, but they
      don't need do a bunch of complex things to extract the token from the
      jar. We can just read it from the jar at any moment via the new
      "Client::getToken()" function instead.

    - "Client::getMappedResponseObject()" no longer has the ability to
      override what's going to be put in the "getFullResponse()" property.
      The full response value now always contains the literal full server
      object. The ability to override it made no sense and was only used in
      a few places, by the messy old way of extracting the token.

    - "Client::api()" no longer constantly scans the cookie jar after every
      request (it was looking for the csrftoken). And it no longer returns
      an array with ugly 0 (token) and 1 (response) elements. Instead, it
      now directly returns just the decoded response, since we have a better
      way of getting the token now! :-)