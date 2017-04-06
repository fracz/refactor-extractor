commit b74a887cd93d5ba864015e899cca790999262f65
Merge: efff757 077a089
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Nov 22 18:42:00 2013 +0100

    minor #9487 unify constructor initialization style throughout symfony (Tobion)

    This PR was merged into the master branch.

    Discussion
    ----------

    unify constructor initialization style throughout symfony

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | n/a

    In almost all classes symfony uses property initialization when the value is static. Constructor initialization is only used for things that actually have logic, like passed parameters or dynamic values. IMHO it makes the code much more readable because property definition, phpdoc and default value is in one place. Also one can easily see what the constructor implements for logic like overridden default value of a parent class. Otherwise the real deal is just hidden behind 10 property initializations. One more advantage is that it requires less code. As you can see, the code was almost cut in half (210 additions and 395 deletions).
    I unified it accordingly across symfony. Sometimes it was [not even consistent within one class](https://github.com/symfony/symfony/blob/master/src/Symfony/Component/Config/Definition/BaseNode.php#L32). At the same time I recognized some errors like missing parent constructor call, or undefined properties or private properties that are not even used.

    I then realized that a few Kernel tests were not passing because they were deeply implementation specific like modifying booted flag with a custom `KernelForTest->setIsBooted();`. I improved and refactored the kernel tests in the __second commit__.

    __Third commit__ unifies short ternary operator, e.g. `$foo ?: new Foo()`. __Forth commit__ unifies missing parentheses, e.g. `new Foo()`.

    Commits
    -------

    077a089 unify missing parentheses
    2888594 unify short ternary operator
    2a9daff [HttpKernel] better written kernel tests
    111ac18 unify constructor initialization style throughout symfony