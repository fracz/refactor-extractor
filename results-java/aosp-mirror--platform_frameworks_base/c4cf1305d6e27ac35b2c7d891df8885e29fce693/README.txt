commit c4cf1305d6e27ac35b2c7d891df8885e29fce693
Author: Jim Miller <jaggies@google.com>
Date:   Fri Sep 24 18:29:03 2010 -0700

    Several improvements to RecentActivities:

    It now toggles between show/hide for each tap on the home button.

    Added new bitmap generation for lighting and halo effect while loading.

    Uses new CarouselViewHelper class to manage textures and threading.

    Uses a "real view" to render detail text.

    Activities can now overload onCreateDescription() to show a
    description in Carousel.

    Improved startup and resume speed by posting single event to
    refresh the activity list.

    Change-Id: Id5552da75b9d022d24f599d11358ddababc97006