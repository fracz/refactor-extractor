commit a28151a8afd0ec11231663e1633cfd8a406df837
Author: Bernhard Schussek <bernhard.schussek@symfony-project.com>
Date:   Tue Feb 1 13:45:27 2011 +0100

    [Form] Removed FormFactory and improved the form instantiation process

    With the form factory there was no reasonable way to implement instantiation of custom form classes. So the implementation was changed to let the classes instantiate themselves. A FormContext instance with default settings has to be passed to the creation method. This context is by default configured in the DI container.

            $context = $this->get('form.context');
            // or
            $context = FormContext::buildDefault();
            $form = MyFormClass::create($context, 'author');

    If you want to circumvent this process, you can also create a form manually. Remember that the services stored in the default context won't be available then unless you pass them explicitely.

            $form = new MyFormClass('author');