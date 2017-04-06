commit 2cf50b78017b805c75f8ba134925e3e71d2fea50
Merge: 569e29d 3363832
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Aug 30 16:40:08 2012 +0200

    merged branch Tobion/requirementscheck (PR #5187)

    Commits
    -------

    3363832 extended phpdoc of ConfigurableRequirementsInterface
    5f64503 [Routing] added test for disabled requirements check
    4225869 [Routing] allow disabling the requirements check on URL generation

    Discussion
    ----------

    [Routing] allow disabling the requirements check on URL generation

    This adds the possibility to disable the requirements check (`strict_requirements = null`) on URL generation.

    To sum up, here the possibilities:
    - `strict_requirements = true`: throw exception for mismatching requirements (most useful in development environment).
    - `strict_requirements = false`: don't throw exception but return null as URL for mismatching requirements and log it (useful when you cannot control all params because they come from third party libs or something but don't want to have a 404 in production environment. it logs the mismatch so you can review it).
    - `strict_requirements = null`: Return the URL with the given parameters without checking the requirements at all. When generating an URL you should either trust your params or you validated them beforehand because otherwise it would break your link anyway (just as with strict_requirements=false). So in production environment you should know that params allways pass the requirements. Thus you could disable the check on each URL generation for performance reasons. If you have 300 links on a page and each URL at least one param you safe 300 unneeded `preg_match` calls. I tested the performance in one of my projects. The rendering time of a single template that contains ~300 links with several params was reduced from avg. 46ms to avg. 42ms. That are 8.7% reduction in the twig layer where the links are created on each request. So this option combines the improved usability of strict_requirements=false with an additional increased performance.

    ---------------------------------------------------------------------------

    by fabpot at 2012-08-30T13:40:38Z

    Can you put the comment about all the possibilities you have mentioned here in the phpdoc for future reference? Thanks.

    ---------------------------------------------------------------------------

    by Tobion at 2012-08-30T13:49:25Z

    In `ConfigurableRequirementsInterface` or which phpdoc would you like to have it? Because `ConfigurableRequirementsInterface` already has it explained. But I can extend its description if thats what you mean.

    ---------------------------------------------------------------------------

    by fabpot at 2012-08-30T13:50:40Z

    The comment in the PR is more explicit and more detailed than the one in the interface. So, yes, basically, it would be great if you can move all the information in the interface phpdoc.

    ---------------------------------------------------------------------------

    by Tobion at 2012-08-30T14:35:59Z

    Done.