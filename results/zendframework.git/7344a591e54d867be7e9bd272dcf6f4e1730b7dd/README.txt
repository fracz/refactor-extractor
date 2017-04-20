commit 7344a591e54d867be7e9bd272dcf6f4e1730b7dd
Author: Evan Coury <me@evancoury.com>
Date:   Fri Nov 11 21:55:45 2011 -0700

    Add install/upgrade interfaces, update config/abstract listener

    - Began porting AutoDependencyManager features into individual listeners and
    interfaces.
    - Use default empty ListenerOption object for default listners if not set.
    - Config listner improved / updated
    - Add writeArryToFile() to AbstractListener