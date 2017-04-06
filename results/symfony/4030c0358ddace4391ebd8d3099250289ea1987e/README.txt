commit 4030c0358ddace4391ebd8d3099250289ea1987e
Merge: 384995e b66ea5e
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Aug 24 03:54:03 2016 -0700

    bug #19601 [FrameworkBundle] Added friendly exception when constraint validator class does not exist (yceruto)

    This PR was submitted for the master branch but it was merged into the 2.7 branch instead (closes #19601).

    Discussion
    ----------

    [FrameworkBundle] Added friendly exception when constraint validator class does not exist

    | Q             | A
    | ------------- | ---
    | Branch?       | master
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    Currently when mistakenly we type a [Custom Constraint Validator class](http://symfony.com/doc/current/validation/custom_constraint.html#creating-the-validator-itself) or the "alias name" from [validator service](http://symfony.com/doc/current/validation/custom_constraint.html#constraint-validators-with-dependencies)  (which would occurs frequently for newcomers) is shown `ClassNotFoundException`:

    > Attempted to load class "alias_name" from namespace "Symfony\Component\Validator".
    Did you forget a "use" statement for another namespace?
    500 Internal Server Error - ClassNotFoundException

    **This PR tries to improve the error message when this happen.**

    But I'm not sure about the exception class used ([`InvalidArgumentException`](https://github.com/yceruto/symfony/blob/master/src/Symfony/Component/Validator/Exception/InvalidArgumentException.php)) : ?

    *  [`ConstraintDefinitionException`](https://github.com/yceruto/symfony/blob/master/src/Symfony/Component/Validator/Exception/ConstraintDefinitionException.php) would be another option, because the source of the error comes from the custom [`Constraint`](https://github.com/yceruto/symfony/blob/master/src/Symfony/Bundle/FrameworkBundle/Validator/ConstraintValidatorFactory.php#L68) definition.

    The text message more convenient from this little mistake: ?

    * This mistake happen for two reason: FQCN or alias name supplied by constraint not found.
    * The constraint validator service was declared incorrectly (missing alias)
    * Perhaps some hint how the developer should resolve the mistake.

    Maybe some documentation core member would help me ?

    Commits
    -------

    b66ea5e added friendly exception when constraint validator does not exist or it is not enabled