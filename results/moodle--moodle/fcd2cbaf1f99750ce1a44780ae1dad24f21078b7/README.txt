commit fcd2cbaf1f99750ce1a44780ae1dad24f21078b7
Author: Petr Skoda <commits@skodak.org>
Date:   Sat Mar 12 17:42:52 2011 +0100

    MDL-26697 multiple media filtering fixes and improvements

    Bug fixes:
        * fixed broken flash resizing via URL
        * upgraded Flowplayer
        * fixed invalid context in format_text()
        * all media related CSS moved from themes to filter and resources
        * fixed automatic pdf resizing in resources

    Changes:
        * reworked filter_mediaplugin system settings - grouped by player type instead of individual extensions, added more information
        * improved regex url matching
        * removed old unused players, Eolas fix and UFO embedding
        * image embedding moved to filter_urltolink
        * new Flowplayer embedding API
        * accessibility and compatibility tweaks in Flowplayer
        * SWF embedding now works only in trusted texts, it is now enabled by default (works everywhere if "Allow EMBED and OBJECT tags" enabled)
        * new default video width and height

    New features:
        * automatic Flash video resizing using information from video metadata
        * Flash HD video support (*.f4v)
        * Flash video embedding with HTML5 fallback - compatible with iOS and other mobile devices
        * Vimeo embedding
        * no-cookie YouTube site supported
        * HTML 5 audio and video with multiple source URLs and QuickTime fallback
        * more video and audio extensions in filelib.php
        * MP3 player colours customisable via CSS in themes
        * nomediaplugin class in a tag prevents media embedding