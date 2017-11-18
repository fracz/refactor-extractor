commit 32826c168638f516bdb764b28c90d7bf3f9f5c13
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Mon Mar 13 13:04:06 2017 +0300

    Introduce LanguageFeature.State, drop coroutines-related pseudofeatures

    Previously there were three LanguageFeature instances -- Coroutines,
    DoNotWarnOnCoroutines and ErrorOnCoroutines -- which were handled very
    awkwardly in the compiler and in the IDE to basically support a language
    feature with a more complex state: not just enabled/disabled, but also
    enabled with warning and enabled with error. Introduce a new enum
    LanguageFeature.State for this and allow LanguageVersionSettings to get
    the state of any language feature with 'getFeatureSupport'.

    One noticeable drawback of this approach is that looking at the API, one
    may assume that any language feature can be in one of the four states
    (enabled, warning, error, disabled). This is not true however; there's
    only one language feature at the moment (coroutines) for which these
    intermediate states (warning, error) are handled in any way. This may be
    refactored further by abstracting the logic that checks the language
    feature availability so that it would work exactly the same for any
    feature.

    Another issue is that the difference among ENABLED_WITH_ERROR and
    DISABLED is not clear. They are left as separate states because at the
    moment, different diagnostics are reported in these two cases and
    quick-fixes in IDE rely on that