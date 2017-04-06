commit d47571f5ec0505c9d04e6720e531dd8d5cd1fbc5
Merge: 7259d4e 773eca7
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Feb 17 10:45:15 2017 -0800

    feature #21530 [DependencyInjection] Add "instanceof" section for local interface-defined configs (nicolas-grekas, dunglas)

    This PR was merged into the 3.3-dev branch.

    Discussion
    ----------

    [DependencyInjection] Add "instanceof" section for local interface-defined configs

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

    This is a direction follow up of #21357 on which we're working together with @dunglas. From the description posted there:

    There is some work being done to include features of [DunglasActionBundle](https://github.com/dunglas/DunglasActionBundle) in the core of Symfony. The goal of all those PRs is to improve the developper experience of the framework, allow to develop faster while preserving all benefits of using Symfony (strictness, modularity, extensibility...) and make it easier to learn for newcomers.

    This PR implements the tagging feature of ActionBundle in a more generic way. It will help to get rid of `AppBundle` in the the standard edition and to register automatically some classes including commands.

    Here is an example of config (that can be embedded in the standard edition) to enable those features:

    ```yaml
    # config/services.yml
    services:
        _defaults:
            autowire: ['get*', 'set*'] # Enable constructor, getter and setter autowiring for all services defined in this file

        _instanceof:
            Symfony\Component\Console\Command: # Add the console.command tag to all services defined in this file having this type
                tags: ['console.command']
                # Set tags but also other settings like "public", "autowire" or "shared" here

            Twig_ExtensionInterface:
                tags: ['twig.extension']

            Symfony\Component\EventDispatcher\EventSubscriberInterface:
                tags: ['kernel.event_subscriber']

        App\: # Register all classes in the src/Controller directory as services
            psr4: ../src/{Controller,Command,Twig,EventSubscriber}
    ```

    It's part of our 0 config initiative: controllers and commands will be automatically registered as services and "autowired", allowing the user to create and inject new services without having to write a single line of YAML or XML.
    When refactoring changes are also automatically updated and don't require to update config files. It's a big win for rapid application development and prototyping.

    Of course, this is fully compatible with the actual way of defining services and it's possible to switch (or mix) approaches very easily. It's even possible to start prototyping using 0config features then switch to explicit services definitions when the project becomes mature.

    Commits
    -------

    773eca7794 [DependencyInjection] Tests + refacto for "instanceof" definitions
    2fb601983f [DependencyInjection] Add "instanceof" section for local interface-defined configs