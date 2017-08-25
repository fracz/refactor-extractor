<?php
namespace Robo;

class TaskException extends \Exception  {

    public function __construct($class, $message)
    {
        if (is_object($class)) {
            $class = get_class($class);
        }
        parent::__construct("  [$class Task] Exception: \n\n  $message");
    }

}
