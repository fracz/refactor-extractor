commit fc6ed5bee304c48c4aec5cbcbee0c667d0438abc
Merge: f59a0ba 24e0eb6
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Jul 2 01:04:20 2015 +0200

    feature #15141 [DX] [Security] Renamed Token#getKey() to getSecret() (WouterJ)

    This PR was squashed before being merged into the 2.8 branch (closes #15141).

    Discussion
    ----------

    [DX] [Security] Renamed Token#getKey() to getSecret()

    There are 2 very vague parameter names in the authentication process: `$providerKey` and `$key`. Some tokens/providers have the first one, some tokens/providers the second one and some both. An overview:

    | Token | `providerKey` | `key`
    | --- | --- | ---
    | `AnonymousToken` | - | yes
    | `PreAuth...Token` | yes | -
    | `RememberMeToken` | yes | yes
    | `UsernamePasswordToken` | yes | -

    Both names are extremely general and their PHPdocs contains pure no-shit-sherlock-descriptions :squirrel: (like "The key."). This made me and @iltar think it's just an inconsistency and they have the same meaning.
    ...until we dived deeper into the code and came to the conclusion that `$key` has a Security task (while `$providerKey` doesn't really). If it takes people connected to Symfony internals 30+ minutes to find this out, it should be considered for an improvement imo.

    So here is our suggestion: **Rename `$key` to `$secret`**. This explains much better what the value of the string has to be (for instance, it's important that the string is not easily guessable and cannot be found out, according to the Spring docs). It also explains the usage better (it's used as a replacement for credentials and to hash the RememberMeToken).

    **Tl;dr**: `$key` and `$providerKey` are too general names, let's improve DX by renaming them. This PR tackles `$key` by renaming it to `$secret`.

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | yes
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    *My excuse for the completely unrelated branch name*

    Commits
    -------

    24e0eb6 [DX] [Security] Renamed Token#getKey() to getSecret()