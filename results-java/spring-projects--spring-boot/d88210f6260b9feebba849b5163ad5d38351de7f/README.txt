commit d88210f6260b9feebba849b5163ad5d38351de7f
Author: Phillip Webb <pwebb@pivotal.io>
Date:   Wed Nov 4 23:12:26 2015 -0800

    Improve performance with BackgroundPreinitializer

    Add a BackgroundPreinitializer to trigger early initialization in a
    background thread of time consuming tasks. By moving certain
    initialization tasks to background thread and triggering them early
    we can improve the critical path when the application starts. For
    example, Tomcat's MBeanFactory class parses several XML files when
    first loaded. If we trigger the load in a background thread it completes
    before Tomcat actually needs to use it.

    The initial set of initializers included with this commit are:

    - Tomcat MBeanFactory
    - javax.validation
    - Spring's AllEncompassingFormHttpMessageConverter

    See gh-4252