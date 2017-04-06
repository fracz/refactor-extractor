commit c8700f04fb6fb5dc21ac24de8665c0476d6db5ef
Author: Matias Niemelä <matias@yearofmoo.com>
Date:   Thu Apr 2 20:52:30 2015 -0700

    feat($animate): complete refactor of internal animation code

    All of ngAnimate has been rewritten to make the internals of the
    animation code more flexible, reuseable and performant.

    BREAKING CHANGE: JavaSript and CSS animations can no longer be run in
    parallel. With earlier versions of ngAnimate, both CSS and JS animations
    would be run together when multiple animations were detected. This
    feature has now been removed, however, the same effect, with even more
    possibilities, can be achieved by injecting `$animateCss` into a
    JavaScript-defined animation and creating custom CSS-based animations
    from there. Read the ngAnimate docs for more info.

    BREAKING CHANGE: The function params for `$animate.enabled()` when an
    element is used are now flipped. This fix allows the function to act as
    a getter when a single element param is provided.

    ```js
    // < 1.4
    $animate.enabled(false, element);

    // 1.4+
    $animate.enabled(element, false);
    ```

    BREAKING CHANGE: In addition to disabling the children of the element,
    `$animate.enabled(element, false)` will now also disable animations on
    the element itself.

    BREAKING CHANGE: Animation-related callbacks are now fired on
    `$animate.on` instead of directly being on the element.

    ```js
    // < 1.4
    element.on('$animate:before', function(e, data) {
      if (data.event === 'enter') { ... }
    });
    element.off('$animate:before', fn);

    // 1.4+
    $animate.on(element, 'enter', function(data) {
      //...
    });
    $animate.off(element, 'enter', fn);
    ```

    BREAKING CHANGE: There is no need to call `$scope.$apply` or
    `$scope.$digest` inside of a animation promise callback anymore
    since the promise is resolved within a digest automatically (but a
    digest is not run unless the promise is chained).

    ```js
    // < 1.4
    $animate.enter(element).then(function() {
      $scope.$apply(function() {
        $scope.explode = true;
      });
    });

    // 1.4+
    $animate.enter(element).then(function() {
      $scope.explode = true;
    });
    ```

    BREAKING CHANGE: When an enter, leave or move animation is triggered then it
    will always end any pending or active parent class based animations
    (animations triggered via ngClass) in order to ensure that any CSS
    styles are resolved in time.