commit 304e4cca1234da31084df81a433384dcdd214141
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Sat Jun 16 12:17:58 2012 +0400

    Move to GitHub API v3, refactor. IDEA-85769

    * Introduce getRequest() and postRequest() methods as entry points to do a query to the API instead of doREST, which requires releasing an HttpMethod and so on.
    * Return parsed JsonElement from these methods, since Github API returns the response in JSON format.
    * Post request in JSON format as well. Get-methods are not called with parameters yet, so don't bother for them.
    * Use these methods from GithubCreateGistAction which already uses API v3.
    * Introduce getApiUrl to return the correct API url for github as well as for enterprise installations.
    * Introduce GithubUser to encapsulate information about a user.
      Currently it works only for the current user, and stores only its plan to know if private repositories are allowed for the person.
    * Rewrite RepositoryInfo to be a structured holder of the information about a repository, without working with DOM elements and so on. Rename some methods to make their return value more obvious.
    * Add @Nullable, @NotNull, javadocs,  code style fixes.
    * In the GithubShareAction move initial pushing to the background to avoid the modality lock with the dialog asking for the passphrase. This fixes IDEA-86645.