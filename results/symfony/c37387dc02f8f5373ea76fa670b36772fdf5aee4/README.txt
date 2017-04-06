commit c37387dc02f8f5373ea76fa670b36772fdf5aee4
Merge: 0252e4e ccf52ec
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Tue Jul 28 16:07:07 2015 +0200

    Merge branch '2.3' into 2.7

    * 2.3:
      [php7] Fix for substr() always returning a string
      [Security] Do not save the target path in the session for a stateless firewall
      [DependencyInjection] fixed FrozenParameterBag and improved Parameter…

    Conflicts:
            src/Symfony/Component/Debug/Tests/ErrorHandlerTest.php
            src/Symfony/Component/Security/Http/Firewall/ExceptionListener.php