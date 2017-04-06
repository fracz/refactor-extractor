commit 42e3ecbe783ea10b73dd8868583698d4d401d083
Merge: 84ba801 f47d905
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat May 2 17:21:08 2015 +0200

    Merge branch '2.6' into 2.7

    * 2.6: (25 commits)
      [2.6] link to https://symfony.com where possible
      Do not override PHP constants, only use when available
      link to https://symfony.com where possible
      [FrameworkBundle] Added missing log in server:run command
      [Finder] Only use GLOB_BRACE when available
      [HttpFoundation] Allow curly braces in trusted host patterns
      Fix merge
      Fix typo in variable name
      [profiler][security] check authenticated user by tokenClass instead of username.
      [WebProfiler] fix html syntax for input types
      [TwigBundle] Fix deprecated use of FlattenException
      [DependencyInjection] Removed extra strtolower calls
      Use https://symfony.com/search for searching
      [Debug] PHP7 compatibility with BaseException
      [Validator] Fixed Choice when an empty array is used in the "choices" option
      Fixed tests
      [StringUtil] Fixed singularification of 'selfies'
      Fix Portuguese (Portugal) translation for Security
      improved exception when missing required component
      [DependencyInjection] resolve circular reference
      ...

    Conflicts:
            src/Symfony/Bundle/WebProfilerBundle/Resources/views/Collector/config.html.twig
            src/Symfony/Component/Form/README.md
            src/Symfony/Component/Intl/README.md
            src/Symfony/Component/Security/README.md
            src/Symfony/Component/Translation/README.md
            src/Symfony/Component/Validator/README.md