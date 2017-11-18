commit abc24ca09b35f51d1bbf9950bc69d6ed4b8dea43
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Mon Aug 10 10:36:49 2015 +0200

    extremely simplistic listing and editing of role information

    name, description are editable, roles can be deleted if they don't have users assigned to them
    no additional interaction is supported yet

    added type information to UserStore, needed refactoring of how to require the store