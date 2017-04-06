commit 6d1e91a1faecb1f46a781d0e4ce37ed9d3fba3cc
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Jan 18 10:23:49 2011 +0100

    refactored bundle management

    Before I explain the changes, let's talk about the current state.

    Before this patch, the registerBundleDirs() method returned an ordered (for
    resource overloading) list of namespace prefixes and the path to their
    location. Here are some problems with this approach:

     * The paths set by this method and the paths configured for the autoloader
       can be disconnected (leading to unexpected behaviors);

     * A bundle outside these paths worked, but unexpected behavior can occur;

     * Choosing a bundle namespace was limited to the registered namespace
       prefixes, and their number should stay low enough (for performance reasons)
       -- moreover the current Bundle\ and Application\ top namespaces does not
       respect the standard rules for namespaces (first segment should be the
       vendor name);

     * Developers must understand the concept of "namespace prefixes" to
       understand the overloading mechanism, which is one more thing to learn,
       which is Symfony specific;

     * Each time you want to get a resource that can be overloaded (a template for
       instance), Symfony would have tried all namespace prefixes one after the
       other until if finds a matching file. But that can be computed in advance
       to reduce the overhead.

    Another topic which was not really well addressed is how you can reference a
    file/resource from a bundle (and take into account the possibility of
    overloading). For instance, in the routing, you can import a file from a
    bundle like this:

      <import resource="FrameworkBundle/Resources/config/internal.xml" />

    Again, this works only because we have a limited number of possible namespace
    prefixes.

    This patch addresses these problems and some more.

    First, the registerBundleDirs() method has been removed. It means that you are
    now free to use any namespace for your bundles. No need to have specific
    prefixes anymore. You are also free to store them anywhere, in as many
    directories as you want. You just need to be sure that they are autoloaded
    correctly.

    The bundle "name" is now always the short name of the bundle class (like
    FrameworkBundle or SensioCasBundle). As the best practice is to prefix the
    bundle name with the vendor name, it's up to the vendor to ensure that each
    bundle name is unique. I insist that a bundle name must be unique. This was
    the opposite before as two bundles with the same name was how Symfony2 found
    inheritance.

    A new getParent() method has been added to BundleInterface. It returns the
    bundle name that the bundle overrides (this is optional of course). That way,
    there is no ordering problem anymore as the inheritance tree is explicitely
    defined by the bundle themselves.

    So, with this system, we can easily have an inheritance tree like the
    following:

    FooBundle < MyFooBundle < MyCustomFooBundle

    MyCustomFooBundle returns MyFooBundle for the getParent() method, and
    MyFooBundle returns FooBundle.

    If two bundles override the same bundle, an exception is thrown.

    Based on the bundle name, you can now reference any resource with this
    notation:

        @FooBundle/Resources/config/routing.xml
        @FooBundle/Controller/FooController.php

    This notation is the input of the Kernel::locateResource() method, which
    returns the location of the file (and of course it takes into account
    overloading).

    So, in the routing, you can now use the following:

        <import resource="@FrameworkBundle/Resources/config/internal.xml" />

    The template loading mechanism also use this method under the hood.

    As a bonus, all the code that converts from internal notations to file names
    (controller names: ControllerNameParser, template names: TemplateNameParser,
    resource paths, ...) is now contained in several well-defined classes. The
    same goes for the code that look for templates (TemplateLocator), routing
    files (FileLocator), ...

    As a side note, it is really easy to also support multiple-inheritance for a
    bundle (for instance if a bundle returns an array of bundle names it extends).
    However, this is not implemented in this patch as I'm not sure we want to
    support that.

    How to upgrade:

     * Each bundle must now implement two new mandatory methods: getPath() and
       getNamespace(), and optionally the getParent() method if the bundle extends
       another one. Here is a common implementation for these methods:

        /**
         * {@inheritdoc}
         */
        public function getParent()
        {
            return 'MyFrameworkBundle';
        }

        /**
         * {@inheritdoc}
         */
        public function getNamespace()
        {
            return __NAMESPACE__;
        }

        /**
         * {@inheritdoc}
         */
        public function getPath()
        {
            return strtr(__DIR__, '\\', '/');
        }

     * The registerBundleDirs() can be removed from your Kernel class;

     * If your code relies on getBundleDirs() or the kernel.bundle_dirs parameter,
       it should be upgraded to use the new interface (see Doctrine commands for
       many example of such a change);

     * When referencing a bundle, you must now always use its name (no more \ or /
       in bundle names) -- this transition was already done for most things
       before, and now applies to the routing as well;

     * Imports in routing files must be changed:
        Before: <import resource="Sensio/CasBundle/Resources/config/internal.xml" />
        After:  <import resource="@SensioCasBundle/Resources/config/internal.xml" />