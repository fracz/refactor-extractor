commit ccc5043bcb9ba8d0ce154d2cbfdf9d30006fa92d
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Tue Apr 15 13:22:51 2014 +0400

    [git] refactor authenticator

    * Handle Git plugin username/password storage in the same matter as for
      other auth providers, but make it the first provider to consider.
    * Save the auth provider which was used for authentication (for future:
      to know where to remove the password from).
    * Check auth providers in askPassword as well (before it was asked
      only in askUserName).

    This is a preparation for IDEA-98189.