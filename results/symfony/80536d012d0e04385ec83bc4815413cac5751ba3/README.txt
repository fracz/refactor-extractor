commit 80536d012d0e04385ec83bc4815413cac5751ba3
Merge: 1033dc5 854e07b
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Sep 4 11:57:59 2014 +0200

    bug #11843 [Yaml] improve error message when detecting unquoted asterisks (xabbuh)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [Yaml] improve error message when detecting unquoted asterisks

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #11835
    | License       | MIT
    | Doc PR        |

    Asterisks in unquoted strings are used in YAML to reference variables. Before Symfony 2.3.19, Symfony 2.4.9 and Symfony 2.5.4, unquoted asterisks in inlined YAML code were treated as regular strings. This was fixed for the inline parser in #11677. However, an unquoted * character now led to an error message like this:

    ```
    PHP Warning:  array_key_exists(): The first argument should be either a string or an integer in vendor/symfony/symfony/src/Symfony/Component/Yaml/Inline.php on line 409

      [Symfony\Component\Yaml\Exception\ParseException]
      Reference "" does not exist at line 171 (near "- { foo: * }").
    ```

    Commits
    -------

    854e07b improve error when detecting unquoted asterisks