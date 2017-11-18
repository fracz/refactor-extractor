commit aa49468171d991845b20f744f17c9a06f6f24052
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Thu Feb 9 10:43:03 2017 +0100

    Allow to define a custom MessageRecoverer

    This commit improves `SimpleRabbitListenerContainerFactoryConfigurer` to
    use a custom `MessageConverter`. If such a bean is present, it is used
    for the default factory that is auto-configured.

    Closes gh-8194