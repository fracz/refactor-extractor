commit 6ec16dfd8ed27b298a45d36124975556487e9e6a
Author: Matthieu Napoli <matthieu@mnapoli.fr>
Date:   Wed Jun 7 09:12:09 2017 +0200

    Compile factories

    Factory definitions are now dumped into the compiled container!

    This follows #494 where all definitions but factories, decorators and wildcards were compiled. Decorators and wildcards are still not compiled.

    Compiling factories means also compiling closures. That was actually much more doable than I thought, I've used [Roave/BetterReflection](https://github.com/Roave/BetterReflection) (thanks asgrim and ocramius!)

    The main downside of the whole pull request is the number of dependencies that BetterReflection brings:

      - Installing symfony/polyfill-util (v1.4.0) Loading from cache
      - Installing symfony/polyfill-php56 (v1.4.0) Loading from cache
      - Installing nikic/php-parser (v2.1.1) Loading from cache
      - Installing jeremeamia/superclosure (2.3.0) Loading from cache
      - Installing zendframework/zend-eventmanager (3.1.0) Loading from cache
      - Installing zendframework/zend-code (3.1.0) Loading from cache
      - Installing phpdocumentor/reflection-common (1.0) Loading from cache
      - Installing phpdocumentor/type-resolver (0.2.1) Loading from cache
      - Installing phpdocumentor/reflection-docblock (2.0.5) Loading from cache
      - Installing roave/better-reflection (1.2.0) Loading from cache

    Maybe we could get on with superclosure only. Or else make compiling closures optional (only compile them if BetterReflection is installed…). Also I've seen that there is a v2.0 of BetterReflection in development [but it requires 7.1](https://github.com/Roave/BetterReflection#changes-in-br-20), I have no idea for how long the v1 will be supported (e.g. will it support PHP 7.2). PHP-DI could upgrade to 7.1 at the end of this year but that's not for sure yet.

    Performance improvements: -20% on the factory.php benchmark, [profile](https://blackfire.io/profiles/compare/6692ca3e-521a-49df-96c6-1bf9833c8209/graph), which is very good for a micro-benchmark IMO. I'll test all that in larger scenarios for the complete 6.0 release but it's safe to say this PR is an improvement.

    There are also a lot of micro/not-so-micro optimisations possible here in the future, especially because there are a lot of parameter-guessing done at runtime (you can inject dependencies into the factories and PHP-DI figures it out with type-hints). https://github.com/PHP-DI/Invoker could be compiled in the future (optionally), and simpler scenarios (classic closures) could be much more optimized than that. Work for later! :)

    Because of closures, some scenarios are explicitly not supported:

    - you should not use `$this` inside closures
    - you should not import variables inside the closure using the `use` keyword, like in `function () use ($foo) { ...`
    - you should not define multiple closures on the same line

    But those scenarios don't make sense in container factories written in array config so that's good!