<?php echo sprintf('<?xml version="1.0" encoding="%s" ?>', $view->getCharset())."\n" ?>
<error code="<?php echo $manager->getStatusCode() ?>" message="<?php echo $manager->getStatusText() ?>" />