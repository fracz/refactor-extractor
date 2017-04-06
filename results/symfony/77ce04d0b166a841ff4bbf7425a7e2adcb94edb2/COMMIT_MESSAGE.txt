commit 77ce04d0b166a841ff4bbf7425a7e2adcb94edb2
Merge: d21c934 d858f7b
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Jul 26 15:55:52 2012 +0200

    merged branch bschussek/normalizer-performance (PR #5067)

    Commits
    -------

    d858f7b [OptionsResolver] Optimized previous values of a lazy option not to be evaluated if the second argument is not defined
    8a338cb [OptionsResolver] Micro-optimization
    e659f0e [OptionsResolver] Improved the performance of normalizers

    Discussion
    ----------

    [OptionsResolver] Improved the performance of normalizers

    Bug fix: no
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: -
    Todo: -

    Normalizers are now stored in the Options instance only once. Previously, normalizers were stored in Options upon resolving, which meant that they were added a lot of time if the same resolver was used for many different options arrays.

    This improvement led to an improvement of 30ms on http://advancedform.gpserver.dk/app_dev.php/taxclasses/1

    ---------------------------------------------------------------------------

    by beberlei at 2012-07-26T13:34:23Z

    @bschussek do you have the code for this forms somewhere btw?

    ---------------------------------------------------------------------------

    by bschussek at 2012-07-26T13:54:52Z

    @beberlei https://github.com/stof/symfony-standard/tree/twig_forms