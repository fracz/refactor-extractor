commit e21fa121fd22fee8602c7ac6f8f3b4ef79f69e83
Author: Jonathan Vollebregt <jnvsor@gmail.com>
Date:   Thu Jun 29 21:04:47 2017 +0200

    Kint_Renderer: Add parserPlugins method and hook

    This should improve performance on text-based renderers
    as they won't execute any unneccesary plugins