commit 05ab8bdb01dfb7e94aa0ea2f9a1c5253738f728b
Author: Luke Towers <github@luketowers.ca>
Date:   Wed Nov 16 12:05:44 2016 -0600

    Pass current model to RelationController view & manage scopes

    This improves the extensibility of the relation controller by passing the parent relation model to the query scope that will be applied to both the view and manage options. It allows the use of attributes of the parent relation model in the query scope applied to the relation.

    This is a mirror of october/octobercms#2419, except for the relation controller instead of the record finder widget. If necessary, I can create a case in the test plugin, but if this is simple enough with the added reference to the prior PR to not require a case in the test plugin, that would be simpler for me :)

    @daftspunk, let me know what you think.