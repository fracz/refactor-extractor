commit 333a39cf3034c59fa88f4432f53b3989bbd26eee
Author: uboness <uboness@gmail.com>
Date:   Tue Aug 26 10:39:45 2014 -0700

    Extended ActionFilter to also enable filtering the response side

    Enables filtering the actions on both sides - request and response. Also added a base class for filter implementations (cleans up filters that only need to filter one side)

    Also refactored the filter & filter chain methods to more intuitive names