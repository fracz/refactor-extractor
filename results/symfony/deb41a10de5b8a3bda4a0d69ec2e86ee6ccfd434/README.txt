commit deb41a10de5b8a3bda4a0d69ec2e86ee6ccfd434
Merge: 77a47d3 e7e39e0
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Aug 29 13:44:34 2012 +0200

    merged branch Tobion/formguess (PR #5361)

    Commits
    -------

    e7e39e0 [Form] refactor Guess
    dcbeeb1 [Form] replaced UnexpectedValueException by InvalidArgumentException in Guess

    Discussion
    ----------

    [Form] replaced UnexpectedValueException by InvalidArgumentException in Guess

    BC break: yes

    this is a better fit because the error is a logic exception (that can be detected at compile time, i.e. when writing the code) instead of a runtime exception

    ---------------------------------------------------------------------------

    by bschussek at 2012-08-29T10:51:54Z

    :+1: