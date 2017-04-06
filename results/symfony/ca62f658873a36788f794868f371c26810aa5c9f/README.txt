commit ca62f658873a36788f794868f371c26810aa5c9f
Merge: ed60d39 d4ebbfd
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Sep 19 13:00:34 2013 +0200

    merged branch fabpot/expression-engine (PR #8913)

    This PR was merged into the master branch.

    Discussion
    ----------

    New Component: Expression Language

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #8850, #7352
    | License       | MIT
    | Doc PR        | not yet

    TODO:

     - [ ] write documentation
     - [x] add tests for the new component
     - [x] implement expression support for access rules in the security component
     - [x] find a better character/convention for expressions in the YAML format
     - [x] check the performance of the evaluation mode
     - [x] better error messages in the evaluation mode
     - [x] add support in the Routing
     - [x] add support in the Validator

    The ExpressionLanguage component provides an engine that can compile and
    evaluate expressions.

    An expression is a one-liner that returns a value (mostly, but not limited to, Booleans).

    It is a strip-down version of Twig (only the expression part of it is
    implemented.) Like Twig, the expression is lexed, parsed, and
    compiled/evaluated. So, it is immune to external injections by design.

    If we compare it to Twig, here are the main big differences:

     * only support for Twig expressions
     * no ambiguity for calls (foo.bar is only valid for properties, foo['bar'] is only valid for array calls, and foo.bar() is required for method calls)
     * no support for naming conventions in method calls (if the method is named getFoo(), you must use getFoo() and not foo())
     * no notion of a line for errors, but a cursor (we are mostly talking about one-liners here)
     * removed everything specific to the templating engine (like output escaping or filters)
     * no support for named arguments in method calls
     * only one extension point with functions (no possibility to define new operators, ...)
     * and probably even more I don't remember right now
     * there is no need for a runtime environment, the compiled PHP string is self-sufficient

    An open question is whether we keep the difference betweens arrays and hashes.

    The other big difference with Twig is that it can work in two modes (possible
    because of the restrictions described above):

     * compilation: the expression is compiled to PHP and is self-sufficient
     * evaluation: the expression is evaluated without being compiled to PHP (the node tree produced by the parser can be serialized and evaluated afterwards -- so it can be saved on disk or in a database to speed up things when needed)

    Let's see a simple example:

    ```php
    $language = new ExpressionLanguage();

    echo $language->evaluate('1 + 1');
    // will echo 2

    echo $language->compile('1 + 2');
    // will echo "(1 + 2)"
    ```

    The language supports:

     * all basic math operators (with precedence rules):
        * unary: not, !, -, +
        * binary: or, ||, and, &&, b-or, b-xor, b-and, ==, ===, !=, !==, <, >, >=, <=, not in, in, .., +, -, ~, *, /, %, **

     * all literals supported by Twig: strings, numbers, arrays (`[1, 2]`), hashes
       (`{a: "b"}`), Booleans, and null.

     * simple variables (`foo`), array accesses (`foo[1]`), property accesses
       (`foo.bar`), and method calls (`foo.bar(1, 2)`).

     * the ternary operator: `true ? true : false` (and all the shortcuts
       implemented in Twig).

     * function calls (`constant('FOO')` -- `constant` is the only built-in
       functions).

     * and of course, any combination of the above.

    The compilation is better for performances as the end result is just a plain PHP string without any runtime. For the evaluation, we need to tokenize, parse, and evaluate the nodes on the fly. This can be optimized by using a `ParsedExpression` or a `SerializedParsedExpression` instead:

    ```php
    $nodes = $language->parse($expr, $names);
    $expression = new SerializedParsedExpression($expr, serialize($nodes));

    // You can now store the expression in a DB for later reuse

    // a SerializedParsedExpression can be evaluated like any other expressions,
    // but under the hood, the lexer and the parser won't be used at all, so it''s much faster.
    $language->evaluate($expression);
    ```
    That's all folks!

    I can see many use cases for this new component, and we have two use cases in
    Symfony that we can implement right away.

    ## Using Expressions in the Service Container

    The first one is expression support in the service container (it would replace
    #8850) -- anywhere you can pass an argument in the service container, you can
    use an expression:

    ```php
    $c->register('foo', 'Foo')->addArgument(new Expression('bar.getvalue()'));
    ```

    You have access to the service container via `this`:

        container.get("bar").getvalue(container.getParameter("value"))

    The implementation comes with two functions that simplifies expressions
    (`service()` to get a service, and `parameter` to get a parameter value). The
    previous example can be simplified to:

        service("bar").getvalue(parameter("value"))

    Here is how to use it in XML:

    ```xml
    <parameters>
        <parameter key="value">foobar</parameter>
    </parameters>
    <services>
        <service id="foo" class="Foo">
            <argument type="expression">service('bar').getvalue(parameter('value'))</argument>
        </service>
        <service id="bar" class="Bar" />
    </services>
    ```

    and in YAML (I chose the syntax randomly ;)):

    ```yaml
    parameters:
        value: foobar

    services:
        bar:
            class: Bar

        foo:
            class: Foo
            arguments: [@=service("bar").getvalue(parameter("value"))]
    ```

    When using the container builder, Symfony uses the evaluator, but with the PHP
    dumper, the compiler is used, and there is no overhead as the expression
    engine is not needed at runtime. The expression above would be compiled to:

    ```php
    $this->get("bar")->getvalue($this->getParameter("value"))
    ```

    ## Using Expression for Security Access Control Rules

    The second use case in Symfony is for access rules.

    As we all know, the way to configure the security access control rules is confusing, which might lead to insecure applications (see http://symfony.com/blog/security-access-control-documentation-issue for more information).

    Here is how the new `allow_if` works:

    ```yaml
    access_control:
        - { path: ^/_internal/secure, allow_if: "'127.0.0.1' == request.getClientIp() or has_role('ROLE_ADMIN')" }
    ```

    This one restricts the URLs starting with `/_internal/secure` to people browsing from the localhost. Here, `request` is the current Request instance. In the expression, there is access to the following variables:

     * `request`
     * `token`
     * `user`

    And to the following functions:

     * `is_anonymous`
     * `is_authenticated`
     * `is_fully_authenticated`
     * `is_rememberme`
     * `has_role`

    You can also use expressions in Twig, which works well with the `is_granted` function:

    ```jinja
    {% if is_granted(expression('has_role("FOO")')) %}
       ...
    {% endif %}
    ```

    ## Using Expressions in the Routing

    Out of the box, Symfony can only match an incoming request based on some pre-determined variables (like the path info, the method, the scheme, ...). But some people want to be able to match on more complex logic, based on other information of the Request object. That's why we introduced `RequestMatcherInterface` recently (but we no default implementation in Symfony itself).

    The first change I've made (not related to expression support) is implement this interface for the default `UrlMatcher`. It was simple enough.

    Then, I've added a new `condition` configuration for Route objects, which allow you to add any valid expression. An expression has access to the `request` and to the routing `context`.

    Here is how one would configure it in a YAML file:

    ```yaml
    hello:
        path: /hello/{name}
        condition: "context.getMethod() in ['GET', 'HEAD'] and request.headers.get('User-Agent') =~ '/firefox/i'"
    ```

    Why do I keep the context as all the data are also available in the request? Because you can also use the condition without using the RequestMatcherInterface, in which case, you don't have access to the request. So, the previous example is equivalent to:

    ```yaml
    hello:
        path: /hello/{name}
        condition: "request.getMethod() in ['GET', 'HEAD'] and request.headers.get('User-Agent') =~ '/firefox/i'"
    ```

    When using the PHP dumper, there is no overhead as the condition is compiled. Here is how it looks like:

    ```php
    // hello
    if (0 === strpos($pathinfo, '/hello') && preg_match('#^/hello/(?P<name>[^/]++)$#s', $pathinfo, $matches) && (in_array($context->getMethod(), array(0 => "GET", 1 => "HEAD")) && preg_match("/firefox/i", $request->headers->get("User-Agent")))) {
        return $this->mergeDefaults(array_replace($matches, array('_route' => 'hello')), array ());
    }
    ```

    Be warned that conditions are not taken into account when generating a URL.

    ## Using Expressions in the Validator

    There is a new Expression constraint that you can put on a class. The expression is then evaluated for validation:

    ```php
    use Symfony\Component\Validator\Constraints as Assert;

    /**
     * @Assert\Condition(condition="this.getFoo() == 'fo'", message="Not good!")
     */
    class Obj
    {
        public function getFoo()
        {
            return 'foo';
        }
    }
    ```

    In the expression, you get access to the current object via the `this` variable.

    ## Dynamic annotations

    The expression language component is also very useful in annotations. the SensoLabs FrameworkExtraBundle leverages this possibility to implement HTTP validation caching in the `@Cache` annotation and to add a new `@Security` annotation (see sensiolabs/SensioFrameworkExtraBundle#238.)

    Commits
    -------

    d4ebbfd [Validator] Renamed Condition to Expression and added possibility to set it onto properties
    a3b3a78 [Validator] added a constraint that runs an expression
    1bcfb40 added optimized versions of expressions
    984bd38 mades things more consistent for the end user
    d477f15 [Routing] added support for expression conditions in routes
    86ac8d7 [ExpressionLanguage] improved performance
    e369d14 added a Twig extension to create Expression instances
    38b7fde added support for expression in control access rules
    2777ac7 [HttpFoundation] added ExpressionRequestMatcher
    c25abd9 [DependencyInjection] added support for expressions in the service container
    3a41781 [ExpressionLanguage] added support for regexes
    9d98fa2 [ExpressionLanguage] added the component