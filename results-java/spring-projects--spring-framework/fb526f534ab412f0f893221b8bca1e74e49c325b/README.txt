commit fb526f534ab412f0f893221b8bca1e74e49c325b
Author: Rossen Stoyanchev <rstoyanchev@vmware.com>
Date:   Thu Sep 22 16:00:22 2011 +0000

    SPR-8700 REFINE ORDER OF ARGUMENT RESOLUTION AND RETURN VALUE HANDLING.

    1. Consider single-purpose return value types like HttpEntity, Model,
    View, and ModelAndView ahead of annotations like @ResponseBody and
    @ModelAttribute. And reversely consider multi-purpose return value
    types like Map, String, and void only after annotations like
    @RB and @MA.

    2. Order custom argument resolvers and return value handlers after the
    built-in ones also clarifying the fact they cannot be used to override
    the built-in ones in Javadoc throughout.

    3. Provide hooks in RequestMappingHandlerAdapter that subclasses can use
    to programmatically modify the list of argument resolvers and return
    value handlers, also adding new getters so subclasses can get access
    to what they need for the override.

    4. Make SessionStatus available through ModelAndViewContainer and
    provide an argument resolver for it.

    5. Init test and javadoc improvements.