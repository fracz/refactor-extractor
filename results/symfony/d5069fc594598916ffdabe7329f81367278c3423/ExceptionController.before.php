<?php

namespace Symfony\Bundle\FrameworkBundle\Controller;

use Symfony\Bundle\FrameworkBundle\Controller;
use Symfony\Bundle\FrameworkBundle\Debug\ExceptionFormatter;
use Symfony\Components\HttpFoundation\Request;
use Symfony\Components\HttpFoundation\Response;
use Symfony\Components\HttpKernel\Exception\HttpException;

/*
 * This file is part of the Symfony framework.
 *
 * (c) Fabien Potencier <fabien.potencier@symfony-project.com>
 *
 * This source file is subject to the MIT license that is bundled
 * with this source code in the file LICENSE.
 */

/**
 * ExceptionController.
 *
 * @author     Fabien Potencier <fabien.potencier@symfony-project.com>
 */
class ExceptionController extends Controller
{
    /**
     * Converts an Exception to a Response.
     *
     * @param \Exception $exception An Exception instance
     * @param Request    $request   The original Request instance
     * @param array      $logs      An array of logs
     *
     * @throws \InvalidArgumentException When the exception template does not exist
     */
    public function exceptionAction(\Exception $exception, Request $originalRequest, array $logs)
    {
        $template = $this->container->getParameter('kernel.debug') ? 'exception' : 'error';

        $format = $format = $originalRequest->getRequestFormat();

        // when using CLI, we force the format to be TXT
        if (0 === strncasecmp(PHP_SAPI, 'cli', 3)) {
            $format = 'txt';
        }

        $template = $this['templating']->getLoader()->load($template, array(
            'bundle'     => 'FrameworkBundle',
            'controller' => 'Exception',
            'format'     => '.'.$format,
        ));

        if (false === $template) {
            throw new \InvalidArgumentException(sprintf('The exception template for format "%s" does not exist.', $format));
        }

        $code      = $exception instanceof HttpException ? $exception->getCode() : 500;
        $text      = Response::$statusTexts[$code];
        $formatter = new ExceptionFormatter($this->container->getParameterBag()->has('debug.file_link_format') ? $this->container->getParameter('debug.file_link_format') : null, $this->container->getParameter('kernel.charset'));
        $message   = null === $exception->getMessage() ? 'n/a' : $exception->getMessage();
        $name      = get_class($exception);
        $traces    = $formatter->getTraces($exception, 'html' === $format ? 'html' : 'text');
        $charset   = $this->container->getParameter('kernel.charset');

        $errors = 0;
        foreach ($logs as $log) {
            if ('ERR' === $log['priorityName']) {
                ++$errors;
            }
        }

        $currentContent = '';
        while (false !== $content = ob_get_clean()) {
            $currentContent .= $content;
        }

        ob_start();
        require $template;
        $content = ob_get_clean();

        $response = $this['response'];
        $response->setStatusCode($code);
        $response->setContent($content);

        return $response;
    }
}