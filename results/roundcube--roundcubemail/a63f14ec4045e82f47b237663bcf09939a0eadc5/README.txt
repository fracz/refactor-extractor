commit a63f14ec4045e82f47b237663bcf09939a0eadc5
Author: Aleksander Machniak <alec@alec.pl>
Date:   Sat Aug 29 07:52:57 2015 +0200

    Emoticons-related code refactoring

    - Emoticons: All emoticons-related functionality is handled by the plugin now
    - Emoticons: Added option to switch on/off emoticons in compose editor (#1485732)
    - Emoticons: Added option to switch on/off emoticons in plain text messages
    - Plugin API: Added disabled_plugins an disabled_buttons options in html_editor hook
    - Plugin API: Added html2text hook