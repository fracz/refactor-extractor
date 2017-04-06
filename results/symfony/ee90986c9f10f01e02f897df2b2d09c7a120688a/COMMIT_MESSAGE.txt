commit ee90986c9f10f01e02f897df2b2d09c7a120688a
Merge: d5ff238 1858b96
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Nov 24 13:18:53 2012 +0100

    merged branch bschussek/drupal-validator (PR #6096)

    This PR was merged into the master branch.

    Commits
    -------

    1858b96 [Form] Adapted FormValidator to latest changes in the Validator
    1f752e8 [DoctrineBridge] Adapted UniqueValidator to latest changes in the Validator
    efe42cb [Validator] Refactored the GraphWalker into an implementation of the Visitor design pattern.

    Discussion
    ----------

    [Validator] Refactored the Validator for use in Drupal

    Bug fix: no
    Feature addition: no
    Backwards compatibility break: yes
    Symfony2 tests pass: yes
    Fixes the following tickets: -
    Todo: -
    License of the code: MIT
    Documentation PR: TODO

    Drupal wants to use the Symfony Validator component in their next version. I was talking to @fago recently about the changes that we'd need to make and implemented these changes in this PR. I don't want to rush this, but the deadline is tight, since Drupal feature freeze is on December 1st and @fago needs at least a couple of days to integrate the Validator into Drupal.

    This PR introduces two significant changes:

    * Interfaces were created for all classes that constitute the Validator's API. This is were the PR breaks BC, because `ConstraintValidatorInterface::initialize()` is now type hinted against `ExecutionContextInterface` instead of `ExecutionContext`.

    * The graph walker was refactored into an implementation of the Visitor pattern. This way, the validator was decoupled from the structure of the metadata (class → properties and getter methods) and makes it possible to implement a different metadata structure, as is required by the Drupal Entity API.

    As a consequence of the API change, custom validation code is now much easier to write, because `ValidatorInterface` and `ExecutionContextInterface` share the following set of methods:

    ```php
    interface ValidatorInterface
    {
        public function validate($value, $groups = null, $traverse = false, $deep = false);
        public function validateValue($value, $constraints, $groups = null);
        public function getMetadataFor($value);
    }

    interface ExecutionContextInterface
    {
        public function validate($value, $subPath = '', $groups = null, $traverse = false, $deep = false);
        public function validateValue($value, $constraints, $subPath = '', $groups = null);
        public function getMetadataFor($value);
    }
    ```

    No more juggling with property paths, no more fiddling with the graph walker. Just call on the execution context what you'd call on the validator and you're done.

    There are two controversial things to discuss and decide (cc @fabpot):

    * I moved the `@api` tags of all implementations to the respective interfaces. Is this ok?
    * I would like to deprecate `ValidatorInterface::getMetadataFactory()` (tagged as `@api`) in favor of the added `ValidatorInterface::getMetadataFor()`, which offers the exact same functionality, but with a different API and better encapsulation, which makes it easier to maintain for us. We can tag `getMetadataFor()` as `@api`, as I don't expect it to change. Can we do this or should we leave the old method in?

    I would like to decide the major issues of this PR until **Sunday November 25th** in order to give @fago enough room for his implementation.

    Let me hear your thoughts.