commit 1570db964665a6d947063d53cf472199ecb51003
Merge: ddaa591 1474aa5
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Jul 18 08:33:15 2012 +0200

    merged branch bschussek/performance (PR #4918)

    Commits
    -------

    1474aa5 [Form] Fixed consideration of Twig's template inheritance and added another performance-improving check
    b4ec7f5 Fixed my rubbish English
    d11f8b5 [Form] Fixed passing of variables in the FormRenderer
    629093e [Form] Extracted common parts of FormHelper and FormExtension into separate classes
    216c539 [Form] Implemented a more intelligent caching strategy in FormHelper (PHP +100ms, Twig +100ms)

    Discussion
    ----------

    [Form] Merged FormHelper and FormExtension and implemented a better caching strategy

    Bug fix: no
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: -
    Todo: -

    This PR extracts common parts of `FormHelper` and `FormExtension` into implementations of the new interfaces `FormRendererInterface` and `FormRendererEngineInterface`. The implemented `AbstractRendererEngine` features a more intelligent caching strategy than the one used before. When this strategy was implemented directly in `FormHelper`, the performance of [this specific, heavy form](http://advancedform.gpserver.dk/app_dev.php/taxclasses/1) could be improved from **2.5** to **2.25 seconds** on my machine for PHP templates.

    Due to the abstraction and delegation, the performance gain is not that big anymore, but we still have a performance gain of about **0.1 seconds** for both PHP and Twig in the above example. The second, big improvement of this PR is maintainability - the differences between PHP and Twig templates are now contained in relatively small classes - and extendability (it is very easy now to support different template engines).

    ---------------------------------------------------------------------------

    by stof at 2012-07-14T13:47:19Z

    should a similar refactoring be done for the [Twig rendering](https://github.com/symfony/symfony/blob/master/src/Symfony/Bridge/Twig/Extension/FormExtension.php) ?

    ---------------------------------------------------------------------------

    by bschussek at 2012-07-14T13:49:25Z

    Yes. I would like to merge the common parts of Twig's FormExtension and PHP's FormHelper into an abstract class. Before that I need to have a [working, heavy Twig Form](https://twitter.com/webmozart/status/224135287377371138) in order to measure whether I don't actually decrease the performance with Twig. Can you help me there?

    ---------------------------------------------------------------------------

    by vicb at 2012-07-16T21:48:24Z

    Would it make sense to create a 'renderer' folder in the form component and move related classes there ?

    ---------------------------------------------------------------------------

    by stof at 2012-07-16T22:06:58Z

    @vicb It makes sense to keep the Twig renderer in the brisge. This is what the bridge is about. Moving the Twig class to the component would not be consistent. And the PHP renderer is already in the component (but it could make sense to move the helper from FrameworkBundle to the TemplatingExtension of the Form component though)

    ---------------------------------------------------------------------------

    by vicb at 2012-07-16T22:16:50Z

    @stof I was only referring to the classes located in the Component/Form folder.

    ---------------------------------------------------------------------------

    by vicb at 2012-07-16T22:27:27Z

    Overall I don't really know what to think of this PR. PHP and Twig use a different way to support blocks:

    - PHP has one block per file,
    - Twig could have many blocks per templates.

    I am not sure if this PR is optimal for Twig and improves maintainability ?

    ---------------------------------------------------------------------------

    by stof at 2012-07-16T22:46:11Z

    @vicb it avoids duplicating the whole rendering logic for each engine (there is at least a third one in [SmartyBundle](https://github.com/noiselabs/SmartyBundle/blob/master/Extension/FormExtension.php) btw)

    ---------------------------------------------------------------------------

    by bschussek at 2012-07-17T07:16:42Z

    @vicb I don't think a renderer subfolder makes sense. The interfaces belong to the main namespace, and then the subfolder would only contain two classes.

    Considering maintainability for Twig, I think that this PR in fact increases it. TwigExtension before always had to check the whole type hierarchy, while now the code in AbstractRendererEngine makes sure that this process is speeded up.

    Before:
    ```
    load _some_entity_field_label:
        - check _some_entity_field_label
        - check entity_label
        - check choice_label
        - check form_label

    load _some_other_entity_field_label
        - check _some_other_entity_field_label
        - check entity_label
        - check choice_label
        - check form_label

    a.s.o.
    ```

    After:
    ```
    load _some_entity_field_label:
        - check _some_entity_field_label
        - check entity_label (hits the cache if entity_label was checked before)
        - check choice_label (hits the cache if choice_label was checked before)
        - check form_label

    load _some_other_entity_field_label
        - check _some_other_entity_field_label
        - check entity_label (now definitely hits the cache)

    a.s.o.
    ```

    Since many fields share the same ancestors in the inheritance tree, this definitely improves performance.

    As can also be deducted here, custom block names such as `_some_entity_field_label` are now a major drawback. There is nothing we can cache for them, so they need to be checked for every individual block that we load. Removing this feature surprisingly gains no performance for Twig (I need to investigate why at some point), but it speeds up rendering for **250ms** using the PHP engine on [this example form](advancedform.gpserver.dk/app_dev.php/taxclasses/1), dropping the rendering time from 1.25 to 1 sec on my local machine. I'm not sure what we should do here.

    ---------------------------------------------------------------------------

    by stof at 2012-07-17T07:21:31Z

    @bschussek could it be possible to have an implementation checking the custom block and another one skipping it ? This way, the user could disable this feature when he does not need it.

    ---------------------------------------------------------------------------

    by bschussek at 2012-07-17T07:38:34Z

    @stof It would be possible to add a switch to `FormRenderer` that controls whether custom blocks are checked or not.

    If this switch is disabled by default, we break BC. If this switch is enabled by default, it will be pretty useless. People will start designing away for custom blocks, and once they want to improve performance, they can't turn off the switch anymore because it would require too many changes.

    ---------------------------------------------------------------------------

    by stof at 2012-07-17T08:08:38Z

    @fabpot what do you think about it ?

    ---------------------------------------------------------------------------

    by bschussek at 2012-07-17T08:41:43Z

    Another option that just came to mind is to remove inheritance checks for anything but _widget and _row. I.e., if we render `entity_widget`, check
    ```
    _id_widget
    entity_widget
    choice_widget
    form_widget
    ```
    But if we render `entity_label`, only check
    ```
    _id_label
    form_label
    ```

    This improves PHP Templating for **170ms** and Twig for **20ms**. We gain another **150ms** for PHP Templating and **~15ms** for Twig if we also restrict custom fields (_id_widget) to the _widget and _row suffixes (it's really hard to tweak the renderer for Twig.. I think a lot of its performance bottlenecks lie in Twig itself).

    Do you have any data on how often blocks other than _widget and _row are customized for specific types/IDs?

    ---------------------------------------------------------------------------

    by stof at 2012-07-17T09:47:38Z

    Well, I think most of the time other blocks are not even customized based on the type :)

    ---------------------------------------------------------------------------

    by Tobion at 2012-07-17T14:32:39Z

    From my experience rendering the form components individually is easier and more flexible than customizing by ID or type.
    But there are still use cases for customizing like library-like bundles (e.g. Bootstrap).