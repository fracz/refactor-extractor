commit c462c35b95e6d7109f4061f6925f1d9874569253
Author: Jonathon Fowler <fowlerj@usq.edu.au>
Date:   Wed May 7 16:11:23 2014 +1000

    MDL-42531 assign: refactor batch operations to use forms properly

    Avoids a lot of ugly optional_param() use. Also fixes the
    non-functional cancel buttons.