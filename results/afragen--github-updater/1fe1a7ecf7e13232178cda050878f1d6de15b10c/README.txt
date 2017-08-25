commit 1fe1a7ecf7e13232178cda050878f1d6de15b10c
Author: Andy Fragen <andy@thefragens.com>
Date:   Tue Mar 17 16:50:48 2015 -0700

    after refactor of get_local_plugin_meta it's

    better to include this code in get_plugin_meta. This affords greater similarity to get_theme_meta and puts all of like code in single function.

    Initially, this was split because additional APIs required additional loops. Now they don't.