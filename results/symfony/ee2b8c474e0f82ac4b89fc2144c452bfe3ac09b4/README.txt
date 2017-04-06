commit ee2b8c474e0f82ac4b89fc2144c452bfe3ac09b4
Merge: 4b52198 b982883
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Jul 31 16:38:15 2012 +0200

    merged branch bschussek/bootstrap (PR #5112)

    Commits
    -------

    b982883 [Form] Moved FormHelper back to FrameworkBundle
    cb62d05 [Form] [Validator] Fixed issues mentioned in the PR
    2185ca8 [Validator] Added entry point "Validation" for more convenient usage outside of Symfony2
    ed87361 [Form] Moved FormHelper creation to TemplatingExtension
    87ccb6a [Form] Added entry point "Forms" for more convenient usage outside of Symfony

    Discussion
    ----------

    [Form] [Validator] Added more convenient entry points for stand-alone usage

    Bug fix: no
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: -
    Todo: -

    This PR greatly simplifies the usage of the Form and Validator component when used outside of Symfony2. Check out the below code to get an idea about the simplified usage:

    ```php
    <?php

    use Symfony\Component\Validator\Validation;
    use Symfony\Component\Validator\Mapping\Cache\ApcCache;
    use Symfony\Component\Form\Forms;
    use Symfony\Component\Form\Extension\HttpFoundation\HttpFoundationExtension;
    use Symfony\Component\Form\Extension\Csrf\CsrfExtension;
    use Symfony\Component\Form\Extension\Csrf\CsrfProvider\SessionCsrfProvider;
    use Symfony\Component\Form\Extension\Templating\TemplatingExtension;
    use Symfony\Component\Form\Extension\Validator\ValidatorExtension;
    use Symfony\Component\HttpFoundation\Session;
    use Symfony\Component\Templating\PhpEngine;

    $session = new Session();
    $secret = 'V8a5Z97e...';
    $csrfProvider = new SessionCsrfProvider($session, $secret);
    $engine = new PhpEngine(/* ... snap ... */);

    $validator = Validation::createValidator();
    // or
    $validator = Validation::createValidatorBuilder()
        ->addXmlMapping('path/to/mapping.xml')
        ->addYamlMapping('path/to/mapping.yml')
        ->addMethodMapping('loadValidatorMetadata')
        ->enableAnnotationMapping()
        ->setMetadataCache(new ApcCache())
        ->getValidator();

    $formFactory = Forms::createFormFactory();
    // or
    $formFactory = Forms::createFormFactoryBuilder()
        // custom types, if you're too lazy to create an extension :)
        ->addType(new PersonType())
        ->addType(new PhoneNumberType())
        ->addTypeExtension(new FormTypeHelpTextExtension())
        // desired extensions (CoreExtension is loaded by default)
        ->addExtension(new HttpFoundationExtension())
        ->addExtension(new CsrfExtension($csrfProvider))
        ->addExtension(new TemplatingExtension($engine, $csrfProvider, array(
            'FormBundle:Form'
        ))
        ->addExtension(new ValidatorExtension($validator))
        ->getFormFactory();

    $form = $formFactory->createBuilder()
        ->add('firstName', 'text')
        ->add('lastName', 'text')
        ->add('age', 'integer')
        ->add('gender', 'choice', array(
            'choices' => array('m' => 'Male', 'f' => 'Female'),
        ))
        ->getForm();

    if (isset($_POST[$form->getName()])) {
        $form->bind($_POST[$form->getName()]);

        if ($form->isValid()) {
            // do stuff
        }
    }

    return $engine->render('AcmeHelloBundle:Hello:index.html.php', array(
        'form' => $form->createView(),
    ));
    ```

    ---------------------------------------------------------------------------

    by bschussek at 2012-07-30T10:08:42Z

    I should maybe add a comment about the benefits of this change, in case they are not self-explanatory:

    * class construction with default configuration is now a one-liner
    * userland code is decoupled from core implementations → userland code doesn't break if we change constructor signatures
    * easier to understand, since many core classes are now created internally
    * easy to discover the possible settings → just look at (FormFactory|Validator)BuilderInterface
    * usage of custom interface implementations is supported, just like before

    ---------------------------------------------------------------------------

    by fabpot at 2012-07-31T08:18:53Z

    The new syntax is great.

    I have one comment though about this PR about support of PHP as a templating system (support for Twig is provided by the bridge and it was already easy to configure Twig as a templating system for forms -- see Silex for instance).

    The `FormHelper` has been moved into the Form component. This helper is only useful when using the PHP templating system (which is not what we recommend people to use), but the default templates are still in the Framework bundle. So using the Form component as standalone with PHP as a templating system still requires to install the bundle to get access to the default templates. Am I missing something? Do we want to move the PHP templates to the Form component too?

    ---------------------------------------------------------------------------

    by stof at 2012-07-31T08:28:28Z

    @fabpot it is even worse than that: the FormHelper currently uses the theme by using ``$theme . ':' . $block . '.html.php`` IIRC. This is not compatible with the default template name parser of the component expecting a path. And the FrameworkBundle template name parser does not support accessing a template outside a bundle AFAIK.
    So moving the templating to the component would require some refactoring in the FormHelper and the template name parser. However, I think it is worth it. Some people complained that using the form rendering (outside the full-stack framework) was requiring either setting up Twig with the bridge, or adding FrameworkBundle in the project (which means including most of the code of the full-stack framework). Having the Templating rendering in the standalone component could be a great idea

    ---------------------------------------------------------------------------

    by fabpot at 2012-07-31T08:42:53Z

    But then, I don't want to promote the Templating component or the PHP templating system. Twig is always a better alternative and this should be what people use most of the time, PHP being the rare exception.

    Anyway, we are too close from the first 2.1 RC, so any big refactoring will have to wait for 2.2.

    ---------------------------------------------------------------------------

    by stof at 2012-07-31T09:02:10Z

    then maybe we should keep the FormHelper in FrameworkBundle for now as it is tied to the FrameworkBundle template name parser anyway currently.

    ---------------------------------------------------------------------------

    by bschussek at 2012-07-31T14:22:35Z

    > it it is even worse than that: the FormHelper currently uses the theme by using ``$theme . ':' . $block . '.html.php`` IIRC. This is not compatible with the default template name parser of the component expecting a path.

    This is why the templates are still in FrameworkBundle. I think they should be moved too, but then we have to change

    * the default theme to an absolute file path
    * the FrameworkBundle name parser to accept absolute paths

    I think this can wait until 2.2. Baby steps.

    > I don't want to promote the Templating component or the PHP templating system.

    We can both promote Twig while making Templating as easy to use as possible. If people want to use Templating, they probably have a reason. We don't have to make their lives more painful than necessary.

    Btw: Templating is a *lot* faster for rendering forms than Twig. On Denis' form, Templating takes 1.15 seconds while Twig takes 2.

    About moving the helpers, we have two choices:

    * Move each helper to the respective component. This would not require new releases of the Templating component when we add more helpers in other component.

    * Move all helpers to Templating. This does not make that much sense for Form, as then Form has support for Templating (TemplatingRendererEngine) and Templating has support for Form (FormHelper), which is a bit weird. I personally prefer a stacked architecture, where Templating is at the bottom and Form-agnostic, and Form (or any other component) builds upon that.

    I'm fine with both approaches. I'll move FormHelper back to FrameworkBundle, and we can decide for a direction in 2.2.

    ---------------------------------------------------------------------------

    by bschussek at 2012-07-31T14:36:30Z

    Done.