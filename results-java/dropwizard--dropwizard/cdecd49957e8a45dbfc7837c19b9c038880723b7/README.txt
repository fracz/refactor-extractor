commit cdecd49957e8a45dbfc7837c19b9c038880723b7
Author: Artem Prigoda <arteamon@gmail.com>
Date:   Sun Jul 12 23:45:19 2015 +0300

    Improve AuthFactoryProvider

    * Make sure users pass the type inherited from `Principal`
    * Remove unchecked cast warnings
    * Remove unnecessary static class `AuthValueFactory`, which could
    be replaced by an anonymous class.
    * Make `PrincipalClassProvider` a holder class, rather then an interface.
    Doing so, we don't create a new anonymous class in `Binder`.
    * Documentation and style improvements.