commit b77d97a6a3cbe44d40afe12e1f05256c29015d99
Merge: 64f9f7b a5c5ad1
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Fri Mar 10 10:24:40 2017 +0100

    bug #21937 [DependencyInjection] Handle void return types in closure-proxy (pierredup)

    This PR was squashed before being merged into the 3.3-dev branch (closes #21937).

    Discussion
    ----------

    [DependencyInjection] Handle void return types in closure-proxy

    | Q             | A
    | ------------- | ---
    | Branch?       | master
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | N/A
    | License       | MIT
    | Doc PR        | N/A

    I recently got an error when registering an event listener that specifies a `void` return type. Dumping the container generates a closure proxy that always returns a value, which then conflicts with the return type hint.

    E.G the following code is generated (some class names removed for readability)

    ```
    $instance->addListener('kernel.view', /** @closure-proxy ... */ function (...\GetResponseForControllerResultEvent $event): void {
        return ${($_ = isset($this->services[listener']) ? $this->services['listener'] : $this->get('listener')) && false ?: '_'}->onKernelView($event);
    }, 128);
    ```

    This then causes the error `A void function must not return a value in ...`

    So void return types should be handled by removing the `return` inside the closure

    Commits
    -------

    a5c5ad1 [DependencyInjection] Handle void return types in closure-proxy