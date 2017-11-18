commit 5a2e30c18bee91343ca9547930efc678a08b0cac
Author: Rossen Stoyanchev <rstoyanchev@gopivotal.com>
Date:   Fri Sep 27 21:02:17 2013 -0400

    Refactor Resource URL generation and Servlet Filter

    Renamed ResourceUrlMapper to ResourceUrlGenerator and refactored it
    to be configured with Resource-serving HandlerMappings as opposed to
    having them detected in the ApplicationContext through the
    BeanPostProcessor contact.

    Renamed and polished ResourceUrlEncodingFilter to ResourceUrlFilter
    and added tests.