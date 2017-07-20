commit 01695bc654a8c1c8c997c87330b4c6481ea17e69
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Apr 27 23:03:29 2011 +0200

    [DoctrineBundle] refactored event listeners/subscribers to not rely on parameter name conventions

     * Doctrine event subscribers now all use the same "doctrine.event_subscriber" tag. To specify a connection,
       use the "connection" attribute.

     * Doctrine event listeners now all use the same "doctrine.event_listener" tag. To specify a connection,
       use the "connection" attribute.