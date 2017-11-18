commit afb897300b9fd310a9a580b45b6f233e7325edf3
Author: Oleg <legka.legka@gmail.com>
Date:   Tue Mar 8 00:56:08 2016 -0800

    Event driven architecture refactored.
    1. Renamed Message to Event and Event to AbstractEvent
    2. Generified Event and Handler
    3. Updated EventDispatcher to make unsafe configuration impossible
    4. Updated UML diagram accordingly