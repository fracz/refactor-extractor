commit 854e07b1de3cfeefcc5afa50dfa1588bd44d758f
Author: Christian Flothmann <christian.flothmann@xabbuh.de>
Date:   Wed Sep 3 19:52:14 2014 +0200

    improve error when detecting unquoted asterisks

    Asterisks in unquoted strings are used in YAML to reference
    variables. Before Symfony 2.3.19, Symfony 2.4.9 and Symfony 2.5.4,
    unquoted asterisks in inlined YAML code were treated as regular
    strings. This was fixed for the inline parser in #11677. However, an
    unquoted * character now led to an error message like this:

    ```
    PHP Warning:  array_key_exists(): The first argument should be either a string or an integer in vendor/symfony/symfony/src/Symfony/Component/Yaml/Inline.php on line 409

      [Symfony\Component\Yaml\Exception\ParseException]
      Reference "" does not exist at line 171 (near "- { foo: * }").
    ```