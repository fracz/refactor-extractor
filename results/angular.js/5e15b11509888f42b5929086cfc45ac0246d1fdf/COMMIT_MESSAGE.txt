commit 5e15b11509888f42b5929086cfc45ac0246d1fdf
Author: Richard Harrington <rwharrington87@gmail.com>
Date:   Tue Aug 26 01:00:53 2014 -0400

    refactor($injector): remove unused strictDi argument from createInternalInjector

    createInternalInjector does not specify the formal parameter `strictDi`, and instead uses the binding
    from the parent function's formal parameters, making this parameter unnecessary.

    Closes #8771