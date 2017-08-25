<?php

namespace MailSo;

/**
 * @category MailSo
 * @package Base
 */
class Config
{
	/**
	 * @var bool
	 */
	public static $ICONV = true;

	/**
	 * @var bool
	 */
	public static $MBSTRING = true;

	/**
	 * @var bool
	 */
	public static $FixIconvByMbstring = true;

	/**
	 * @var int
	 */
	public static $MessageListCountLimitTrigger = 0;

	/**
	 * @var bool
	 */
	public static $MessageListUndeletedFilter = false;

	/**
	 * @var int
	 */
	public static $MessageListDateFilter = 0;

	/**
	 * @var \MailSo\Log\Logger|null
	 */
	public static $SystemLogger = null;
}