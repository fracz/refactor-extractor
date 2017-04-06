commit 4a1375586cdd62e25987f1f19be05ee25e129309
Merge: d1e246e 7b43827
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Oct 24 07:51:19 2014 +0200

    Merge branch '2.5'

    * 2.5:
      enforce memcached version to be 2.1.0
      [PropertyAccess] Simplified code
      [FrameworkBundle] improve server:run feedback
      [Form] no need to add the url listener when it does not do anything
      [Form] Fix #11694 - Enforce options value type check in some form types
      Lithuanian security translations
      [SecurityBundle] Add trust_resolver variable into expression | Q             | A | ------------- | --- | Bug fix?      | [yes] | New feature?  | [no] | BC breaks?    | [no] | Deprecations? | [no] | Tests pass?   | [yes] | Fixed tickets | [#12224] | License       | MIT | Doc PR        | [-]
      [Router] Cleanup
      Fixed UPGRADE-3.0.md markup
      [FrameworkBundle] Fixed ide links
      Add missing argument
      [TwigBundle] do not pass a template reference to twig
      [TwigBundle] show correct fallback exception template in debug mode
      [TwigBundle] remove unused email placeholder from error page
      use meta charset in layouts without legacy http-equiv

    Conflicts:
            src/Symfony/Bundle/TwigBundle/Controller/ExceptionController.php