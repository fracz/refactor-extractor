<?php

//Enable full error reporting
error_reporting(E_ALL | E_STRICT); ini_set('display_errors', true);
//For E_STRICT you should set this
date_default_timezone_set('Australia/Melbourne');

//Require the injector
require_once dirname(__FILE__) . '/../../lib/swift-required.php';

//Create messages fluidly
$message = Swift_MimeFactory::create('message')
  ->setSubject('A basic message')
  ->setTo(array('chris.corbyn@swiftmailer.org' => 'Chris Corbyn'))
  ->setFrom(array('chris@w3style.co.uk' => 'Myself'))
  ->setContentType('text/plain')
  ->setCharset('utf-8')
  ->setBody('just testing')
  ;

echo $message->toString();