commit 5c760b0d402d1dda2ff4825addb8ff285bbe881d
Merge: 600b8ef 731b28b
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Sep 29 17:27:38 2011 +0200

    merged branch igorw/composer (PR #2275)

    Commits
    -------

    731b28b [composer] add missing deps for FrameworkBundle
    9c8f100 [composer] change ext/intl to the new ext-intl syntax
    d535afe [composer] fix monolog-bridge composer.json, add more inter-component deps
    9ade639 [composer] add composer.json

    Discussion
    ----------

    Composer

    This PR adds a composer.json file for [composer](https://github.com/composer/composer) ([more info](packagist.org/about-composer)).

    For discussion you can also go into #composer-dev on freenode and argue with naderman, seldaek and everzet.

    ---------------------------------------------------------------------------

    by naderman at 2011/09/26 15:51:51 -0700

    You haven't entered any keywords, they might come in handy when searching for packages on packagist.

    But really this is just a +1 ;-)

    ---------------------------------------------------------------------------

    by stof at 2011/09/26 16:12:21 -0700

    See my comments on your previous (non-rebased) commit: https://github.com/igorw/symfony/commit/f1c0242b5ae9137c5519557144e803cf50ae9213

    ---------------------------------------------------------------------------

    by igorw at 2011/09/27 00:04:36 -0700

    Following dependencies do not have a composer.json yet: Twig, Doctrine (orm, dbal, common), swiftmailer.

    Also missing from the standard edition: assetic, twig-extensions, jsm-metadata, SensioFrameworkExtraBundle, JMSSecurityExtraBundle, SensioDistributionBundle, SensioGeneratorBundle, AsseticBundle.

    The point is, those can be added later on. Having the components composerized is already a leap forward. Also, doctrine depends on some symfony components, we've got to start somewhere.

    ---------------------------------------------------------------------------

    by Seldaek at 2011/09/27 00:36:41 -0700

    Also, just for information, the plan is to have `symfony/framework-bundle` be the "framework", with all dependencies to doctrine etc, though we should really only have strict requirements in there, and then in symfony-standard we ship a composer.json that requires the framework-bundle, doctrine-orm and things like that that are not essential to core. Otherwise people don't have a choice about what they use anymore.

    Just a comment btw, the json is invalid, all / should be escaped. However json_decode is nice enough to parse those without complaining, browsers do too, even Crockford's json2.js does, so I'm not sure if we should privilege readability over strictness, since it seems nobody really cares about this escaping.

    ---------------------------------------------------------------------------

    by igorw at 2011/09/27 00:41:39 -0700

    So, I've implemented all of @stof's suggestions, except (for reasons stated above):

    * doctrine to DoctrineBundle
    * swiftmailer to SwiftmailerBundle
    * twig to TwigBundle
    * doctrine-common to Validator
    * FrameworkBundle (what exactly does it depend on?)

    ---------------------------------------------------------------------------

    by stof at 2011/09/27 00:52:31 -0700

    @igorw at least HttpKernel, Routing, Templating, EventDispatcher, Doctrine Common (annotations cannot be disabled), Translator, Form (optional), Validator (optional), Console (optional). See the service definitions to see the others

    @Seldaek FrameworkBundle does not depend on Doctrine, except for Common

    ---------------------------------------------------------------------------

    by beberlei at 2011/09/27 03:15:34 -0700

    What does the symfony/ or ext/ prefix control in composer?

    ---------------------------------------------------------------------------

    by Seldaek at 2011/09/27 03:33:52 -0700

    symfony/ is just the (mandatory) vendor namespace. Also ext/ has been renamed to ext- now, so it's not in any vendor, and should avoid potential issues.

    ---------------------------------------------------------------------------

    by beberlei at 2011/09/27 05:07:03 -0700

    @Seldaek Mandatory? So every package name is "vendor/package"? I like that because previously i thought package names are not namespaced, and thus clashes could occur between different communities easily.

    ---------------------------------------------------------------------------

    by Seldaek at 2011/09/27 05:16:20 -0700

    @beberlei: Mandatory. As of yesterday http://packagist.org/ will tell you you have an invalid package name if there's no slash in it. See https://github.com/composer/packagist/commit/1306d1ca82ecb004a885671b823c9793efa9354b#diff-3