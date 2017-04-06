commit bd07b8919d8250dbbfa4aacdef4b8c7f258bcfa7
Merge: da4cee2 95727ff
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue May 15 10:14:33 2012 +0200

    merged branch bschussek/options (PR #3968)

    Commits
    -------

    95727ff [OptionsResolver] Updated PHP requirements to 5.3.3
    1c5f6c7 [OptionsResolver] Fixed issues mentioned in the PR comments
    d60626e [OptionsResolver] Fixed clear() and remove() method in Options class
    2b46975 [OptionsResolver] Fixed Options::replace() method
    16f7d20 [OptionsResolver] Improved implementation and clarity of the Options class
    6ce68b1 [OptionsResolver] Removed reference to non-existing property
    9c76750 [OptionsResolver] Fixed doc and block nesting
    876fd9b [OptionsResolver] Implemented fluid interface
    95454f5 [OptionsResolver] Fixed typos
    256b708 [OptionsParser] Renamed OptionsParser to OptionsResolver
    04522ca [OptionsParser] Added method replaceDefaults()
    b9d053e [Form] Moved Options classes to new OptionsParser component

    Discussion
    ----------

    Extracted OptionsResolver component out of Form

    Bug fix: no
    Feature addition: yes
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: -
    Todo: -

    ![Travis Build Status](https://secure.travis-ci.org/bschussek/symfony.png?branch=options)

    This PR refactors the options-related code of the Form component into a separate component. See the README file for usage examples.

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-04-17T18:11:03Z

    To me it seems like we have some redundancy with the Config/Definition component. I'm wondering if these two can/should be merged somehow?

    ---------------------------------------------------------------------------

    by kriswallsmith at 2012-04-17T18:14:44Z

    I would also suggest merging this into the Config component. Its current name too closely resembles Python's optparser lib, which could create confusion.

    ---------------------------------------------------------------------------

    by bschussek at 2012-04-17T18:18:49Z

    Merge conflict artifacts are fixed now.

    @schmittjoh Do we? Isn't the idea of the Config component to read complex configuration from different configuration providers? (YAML, XML, Annotations etc.)

    The idea of this parser is to be highly performant and to be usable in simple classes. If this can be achieved with the Config component, I'm happy to learn more.

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-04-17T18:27:08Z

    The config component is basically a super intelligent version of array_merge and the like.

    About performance, I haven't really done any tests to say something about the impact. I think it's safe to say that it would be at least slower than your implementation in its current form due to the additional indirection. However, we could probably add a caching layer.

    ---------------------------------------------------------------------------

    by bschussek at 2012-04-17T18:31:22Z

    Have you checked the README I wrote? Are you sure the Config component is intended for the same purpose and not *way* too complex in this case?

    ---------------------------------------------------------------------------

    by stof at 2012-04-17T18:51:14Z

    You also forgot to update the ``replace`` section of the root composer.json file.

    And regarding doing such thing with the Config Definition stuff, it would be more difficult: it builds the tree of values with their defaults, and then merges stuff coming from different sources. The form component however receives defaults from different places (which also define the allowed keys at the same time) and then receives user options only once. And it needs to handle easily default values which depend from other values. So I think both implementations are useful for different needs (however, we could argue about making it a subnamespace in the Config component, but this would add yet another different stuff in it)

    ---------------------------------------------------------------------------

    by jalliot at 2012-04-17T18:58:03Z

    @bschussek You need to add this component to the main composer.json too.

    ---------------------------------------------------------------------------

    by lsmith77 at 2012-04-18T06:54:17Z

    doesn't this overlap a bit with the ``TreeBuilder`` in the Config component?

    ---------------------------------------------------------------------------

    by lsmith77 at 2012-04-18T06:59:12Z

    ah just saw @stof's comment .. i think the biggest argument against TreeBuilder is that it was designed for a very specific purpose and performance wasn't one of them. where as Form needs something that performs fast. so yeah i do see different use cases, but i don't think this means we should have a new component.

    furthermore while i haven't read the code in details i am surprised it doesn't make use of http://php.net/manual/en/function.array-replace-recursive.php to merge defaults into a user supplied options array.

    ---------------------------------------------------------------------------

    by bschussek at 2012-04-18T08:10:49Z

    @stof, @jalliot: Fixed.

    > furthermore while i haven't read the code in details i am surprised it doesn't make use of http://php.net/manual/en/function.array-replace-recursive.php to merge defaults into a user supplied options array.

    @lsmith77: Because that's not what this component does. The key feature of this component is to resolve default values of options that depend on the *concrete* values of other options. I invite you to read the README.

    Is it a good idea to merge this into Config? I think that both components address different audiences and different purposes. The idea of this one is to initialize classes with simple, run-time provided arrays. The idea of Config is to load and validate complex configurations from storage providers, such as the filesystem.

    ---------------------------------------------------------------------------

    by bschussek at 2012-04-18T08:18:48Z

    Note: Not all relevant code of this component is shown in the diff. The (crucial) Options and LazyOption classes have only been moved out of Form.

    ---------------------------------------------------------------------------

    by lsmith77 at 2012-04-18T08:20:02Z

    > Is it a good idea to merge this into Config? I think that both components address different audiences and different purposes. The idea of this one is to initialize classes with simple, run-time provided arrays. The idea of Config is to load and validate complex configuration values from the filesystem (typically).

    decoupled is all fine, but to me this feels a bit too granular. but i am just expressing a gut feeling here

    ---------------------------------------------------------------------------

    by jalliot at 2012-04-18T08:34:03Z

    I think too it should be included in the config component (maybe in a subnamespace). Indeed the behaviour is too different to be merged into the current component but its purpose is similar and is all about *configuration* (hence the name of the component). Otherwise we could also split the current Config component into smaller components as it seems to me there are already parts of it that are totally unrelated to each other.

    ---------------------------------------------------------------------------

    by bschussek at 2012-04-18T11:30:55Z

    @jalliot Can you go into detail which parts that are and what changes you suggest?

    @kriswallsmith Any other naming suggestion?

    ---------------------------------------------------------------------------

    by jalliot at 2012-04-18T11:34:35Z

    @bschussek I don't know the current component well enough but that's the impression I had last time I looked at its code but I may be wrong.

    ---------------------------------------------------------------------------

    by stof at 2012-04-18T19:30:43Z

    @bschussek the Definition subnamespace of the Config component is standalone. It is not directly related to the Loader part

    ---------------------------------------------------------------------------

    by bschussek at 2012-04-19T09:32:48Z

    @stof So what do you recommend?

    I think this is also a question of marketing. Is the Definition subnamespace intended to be used totally separately of the loaders? What are the use cases? If there are good use cases, it makes sense to me to extract the Definition part into a separate component. Otherwise not.

    It is also a question of marketing, because the purpose of a component should be communicable in simple words (quoting @fabpot). The purpose of Config is (copied from the README):

    > Config provides the infrastructure for loading configurations from different data sources and optionally monitoring these data sources for changes. There are additional tools for validating, normalizing and handling of defaults that can optionally be used to convert from different formats to arrays.

    I think this purpose is completely different than that of OptionsParser.

    ---------------------------------------------------------------------------

    by stof at 2012-04-19T11:39:50Z

    The current description itself shows the current state: what is advocated as the main goal of the component (and was the original part) is the loader stuff. But the Definition part (mentioned as "additional tools") is bigger in term of LOC

    ---------------------------------------------------------------------------

    by bschussek at 2012-04-19T11:55:17Z

    @stof: Yes, this is a fact, but what's your opinion? How do we proceed with this PR?

    ---------------------------------------------------------------------------

    by stof at 2012-04-19T12:21:44Z

    Well, my opinion is that the current Config component may deserve to be split into 2 components (as someone may need only part of it). But this would be a huge BC break. @fabpot what do you think ?

    ---------------------------------------------------------------------------

    by bschussek at 2012-04-23T10:14:57Z

    @fabpot Can we merge this?

    ---------------------------------------------------------------------------

    by fabpot at 2012-05-10T06:45:20Z

    @bschussek I'm +1 for this PR but as mentioned by @kriswallsmith, we must find another name as `OptionsParser` immediately make me think of something related to the CLI.

    ---------------------------------------------------------------------------

    by stof at 2012-05-10T06:47:45Z

    However, after thinking about it again, I would vote for keeping it in its own component instead of adding yet another independant part in Config, to avoid forcing Form users to get the whole Config component

    ---------------------------------------------------------------------------

    by bschussek at 2012-05-10T09:09:36Z

    I'm having difficulties finding a better name. The main difference to CLI option parsers is that these actualy *parse* a string, while this class only receives an array of options (does not do any parsing). Otherwise both have the same purpose.

    A couple of other suggestions:

    * OptionsLoader (likely confused with our filesystem loaders)
    * OptionsResolver
    * OptionsMerger
    * OptionsMatcher (not accurate)
    * OptionsBuilder (likely confused with the builder pattern)
    * OptionsJoiner
    * OptionsBag (likely confused with the session bags)
    * OptionsConfig (likely confused with Config)
    * OptionsDefinition (likely confused with Config\Definition)
    * OptionsSpec
    * OptionsCombiner
    * OptionsInitializer
    * OptionsComposer

    The difficulty is to find a name that best reflects its purpose:

    ```
    $parser->setDefaults(...);
    $parser->setRequired(...);
    $parser->setOptional(...);
    $parser->setAllowedValues(...)
    $parser->parse($userOptions);
    ```

    The only of the above examples that makes sense to me here is OptionsResolver -> resolve($userOptions).

    Ideas?

    ---------------------------------------------------------------------------

    by stof at 2012-05-10T09:56:54Z

    OptionsResolver seems a better name than OptionsParser

    ---------------------------------------------------------------------------

    by luxifer at 2012-05-10T09:59:45Z

    Agree with @stof

    ---------------------------------------------------------------------------

    by r1pp3rj4ck at 2012-05-10T10:03:53Z

    I don't really like the plural in the name, but OptionsResolver seems better than OptionsParser. OptionResolver maybe?

    ---------------------------------------------------------------------------

    by sstok at 2012-05-10T10:10:14Z

    @r1pp3rj4ck Options makes more sense as they can be nested/deeper, and thus are multiple.

    Agree with @stof also.

    ---------------------------------------------------------------------------

    by r1pp3rj4ck at 2012-05-10T10:13:01Z

    @sstok well, we have multiple events too and the name is EventDispatcher, not EventsDispatcher. Actually none of the component names are plural.

    ---------------------------------------------------------------------------

    by newicz at 2012-05-10T10:33:50Z

    OptionsResolver - I find it suggesting that there is some kind of problem to be resolved and there's not,
    maybe OptionsDefiner but it isn't good aswell this is a tough one