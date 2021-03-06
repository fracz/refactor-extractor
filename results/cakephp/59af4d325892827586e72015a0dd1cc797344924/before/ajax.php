<?php
/* SVN FILE: $Id$ */

/**
 * Helper for AJAX operations.
 *
 * Helps doing AJAX using the Prototype library.
 *
 * PHP versions 4 and 5
 *
 * CakePHP :  Rapid Development Framework <http://www.cakephp.org/>
 * Copyright (c) 2005, CakePHP Authors/Developers
 *
 * Author(s): Michal Tatarynowicz aka Pies <tatarynowicz@gmail.com>
 *            Larry E. Masters aka PhpNut <nut@phpnut.com>
 *            Kamil Dzielinski aka Brego <brego.dk@gmail.com>
 *
 *  Licensed under The MIT License
 *  Redistributions of files must retain the above copyright notice.
 *
 * @filesource
 * @author       CakePHP Authors/Developers
 * @copyright    Copyright (c) 2005, CakePHP Authors/Developers
 * @link         https://trac.cakephp.org/wiki/Authors Authors/Developers
 * @package      cake
 * @subpackage   cake.libs.helpers
 * @since        CakePHP v 0.9.2
 * @version      $Revision$
 * @modifiedby   $LastChangedBy$
 * @lastmodified $Date$
 * @license      http://www.opensource.org/licenses/mit-license.php The MIT License
 */


/**
 * AjaxHelper helper library.
 *
 * Helps doing AJAX using the Prototype library.
 *
 * @package    cake
 * @subpackage cake.libs.helpers
 * @since      CakePHP v 0.9.2
 *
 */

uses('helpers/html', 'helpers/javascript');


class AjaxHelper extends Helper
{

	/**
     * Returns link to remote action
     *
     * Returns a link to a remote action defined by <i>options[url]</i>
     * (using the urlFor format) that's called in the background using
     * XMLHttpRequest. The result of that request can then be inserted into a
     * DOM object whose id can be specified with <i>options[update]</i>.
     * Usually, the result would be a partial prepared by the controller with
     * either renderPartial or renderPartialCollection.
     *
     * Examples:
     * <code>
     *  linkToRemote("Delete this post",
     *  		array("update" => "posts", "url" => "delete/{$postid->id}"));
     *  linkToRemote(imageTag("refresh"),
     *		array("update" => "emails", "url" => "list_emails" ));
     * </code>
     *
     * By default, these remote requests are processed asynchronous during
     * which various callbacks can be triggered (for progress indicators and
     * the likes).
     *
     * Example:
     * <code>
     *   linkToRemote (word,
     *       array("url" => "undo", "n" => word_counter),
     *       array("complete" => "undoRequestCompleted(request)"));
     * </code>
     *
     * The callbacks that may be specified are:
     *
     * - <i>loading</i>::       Called when the remote document is being
     *                           loaded with data by the browser.
     * - <i>loaded</i>::        Called when the browser has finished loading
     *                           the remote document.
     * - <i>interactive</i>::   Called when the user can interact with the
     *                           remote document, even though it has not
     *                           finished loading.
     * - <i>complete</i>::      Called when the XMLHttpRequest is complete.
     *
     * If you for some reason or another need synchronous processing (that'll
     * block the browser while the request is happening), you can specify
     * <i>options[type] = synchronous</i>.
     *
     * You can customize further browser side call logic by passing
     * in Javascript code snippets via some optional parameters. In
     * their order of use these are:
     *
     * - <i>confirm</i>::      Adds confirmation dialog.
     * -<i>condition</i>::    Perform remote request conditionally
     *                          by this expression. Use this to
     *                          describe browser-side conditions when
     *                          request should not be initiated.
     * - <i>before</i>::       Called before request is initiated.
     * - <i>after</i>::        Called immediately after request was
     *  		             initiated and before <i>loading</i>.
     *
     * @param HtmlHelper $html 	The HtmlHelper object which is creating link.
     * @param string $title 		Title of link
     * @param array $options 		Options for JavaScript function
     * @param array $html_options 	Options for link
     * @return string 				HTML code for link to remote action
     */
	function linkToRemote ($html, $title, $options = null, $html_options = null)
	{
		// mouse click should call our remote function
		$html_options['onclick'] = $this->remoteFunction($html, $options);

		$href = (!empty($options['fallback'])) ? $options['fallback'] : '#';

		// generate actual link
		return $html->link($title, $href, $html_options);
	}

	/**
      * Creates JavaScript function for remote AJAX call
      *
      * This function creates the javascript needed to make a remote call
      * it is primarily used as a helper for linkToRemote.
      *
      * @see linkToRemote() for docs on options parameter.
      *
      * @param array $options options for javascript
      * @return string html code for link to remote action
      */
	function remoteFunction ($html, $options = null)
	{
		$javascript_options = $this->__optionsForAjax($options);
		$func = isset($options['update']) ? "new Ajax.Updater('{$options['update']}', " : "new Ajax.Request(";

		$func .= "'" . $html->url(isset($options['url']) ? $options['url'] : "") . "'";
		$func .= ", $javascript_options)";

		if (isset($options['before']))
		{
			$func = "{$options['before']}; $function";
		}
		if (isset($options['after']))
		{
			$func = "$func; {$options['before']};";
		}
		if (isset($options['condition']))
		{
			$func = "if ({$options['condition']}) { $func; }";
		}
		if (isset($options['confirm']))
		{
			$js = new JavascriptHelper;
			$func = "if (confirm('" . $js->escapeScript($options['confirm']) . "')) { $func; } else { return false; }";
		}
		return $func;
	}

	/**
      * Periodically call remote url via AJAX.
      *
      * Periodically calls the specified url (<i>options[url]</i>) every <i>options[frequency]</i> seconds (default is 10).
      * Usually used to update a specified div (<i>options[update]</i>) with the results of the remote call.
      * The options for specifying the target with url and defining callbacks is the same as linkToRemote.
      *
      * @param JavascriptHelper $script Script helper generating the block
      * @param array $options 		Callback options
      * @return string 				Javascript code
      */
	function remoteTimer ($script, $options = null)
	{
		$frequency = (isset($options['frequency']))? $options['frequency'] : 10;
		$code = "new PeriodicalExecuter(function() {" . $this->remote_function($options) . "}, $frequency)";
		return $script->codeBlock($code);
	}

	/**
      * Returns form tag that will submit using Ajax.
      *
      * Returns a form tag that will submit using XMLHttpRequest in the background instead of the regular
      * reloading POST arrangement. Even though it's using Javascript to serialize the form elements, the form submission
      * will work just like a regular submission as viewed by the receiving side (all elements available in params).
      * The options for specifying the target with :url and defining callbacks is the same as link_to_remote.
      *
      * @param HtmlHelper $html 		HtmlHelper creating this form
      * @param JavascriptHelper $script JavascriptHelper creating this form
      * @param string $id 				Form id
      * @param array $options 			Callback options
      * @return string 					JavaScript code
      */
	function form ($html, $script, $id, $options = null)
	{
		$options['form'] = true;
		$options['html']['id'] = $id;
		//$options['html']['onsubmit'] = $this->remoteFunction($options) . "; return false;";
		return $html->formTag(null, "post", $options) . $script->event("$('$id')", "submit", "function(){" . $this->remoteFunction($options) . "; return false;}");
	}

	/**
      * Returns a button input tag that will submit using Ajax
      *
      * Returns a button input tag that will submit form using XMLHttpRequest in the background instead of regular
      * reloading POST arrangement. <i>options</i> argument is the same as in <i>form_remote_tag</i>
      *
      * @param HtmlHelper $html		HtmlHelper creating this form
      * @param string $name 		Input button name
      * @param string $value 		Input button value
      * @param array $options 		Callback options
      * @return string 				Ajaxed input button
      */
	function submit ($html, $name, $value, $options = null)
	{
		$options['with'] = 'Form.serialize(this.form)';
		$options['html']['type'] = 'button';
		$options['html']['onclick'] = $this->remoteFunction($html, $options) . "; return false;";
		$options['html']['name'] = $name;
		$options['html']['value'] = $value;
		return $html->tag("input", $options['html'], false);
	}

	/**
      * Observe field and call ajax on change.
      *
      * Observes the field with the DOM ID specified by <i>field_id</i> and makes
      * an Ajax when its contents have changed.
      *
      * Required +options+ are:
      * - <i>frequency</i>:: The frequency (in seconds) at which changes to
      *                       this field will be detected.
      * - <i>url</i>::       @see urlFor() -style options for the action to call
      *                       when the field has changed.
      *
      * Additional options are:
      * - <i>update</i>::    Specifies the DOM ID of the element whose
      *                       innerHTML should be updated with the
      *                       XMLHttpRequest response text.
      * - <i>with</i>::      A Javascript expression specifying the
      *                       parameters for the XMLHttpRequest. This defaults
      *                       to Form.Element.serialize('$field_id'), which can be
      *                       accessed from params['form']['field_id'].
      *
      * Additionally, you may specify any of the options documented in
      * @see linkToRemote().
      *
      * @param JavaScriptHelper $script
      * @param string $field_id 		DOM ID of field to observe
      * @param array $options 			ajax options
      * @return string 					ajax script
      */
	function observeField ($html, $script, $field_id, $options = null)
	{
		if (!isset($options['with']))
		{
			$options['with'] = "Form.Element.serialize('$field_id')";
		}
		return $script->codeBlock($this->__buildObserver($html, 'Form.Element.Observer', $field_id, $options));
	}

	/**
      * Observe entire form and call ajax on change.
      *
      * Like @see observeField(), but operates on an entire form identified by the
      * DOM ID <b>form_id</b>. <b>options</b> are the same as <b>observe_field</b>, except
      * the default value of the <i>with</i> option evaluates to the
      * serialized (request string) value of the form.
      *
      * @param HtmlHelper $html
      * @param JavaScriptHelper $script
      * @param string $field_id 	DOM ID of field to observe
      * @param array $options 		ajax options
      * @return string 				ajax script
      */
	function observeForm ($html, $script, $field_id, $options = array())
	{
		if (!isset($options['with']))
		{
			$options['with'] = 'Form.serialize(this.form)';
		}
		return $script->codeBlock($this->__buildObserver($html, 'Form.Observer', $field_id, $options));
	}

	/**
      * Create a text field with Autocomplete.
      *
      * Creates an autocomplete field with the given ID and options.
      *
      * options['with'] defaults to "Form.Element.serialize('$field_id')",
      * but can be any valid javascript expression defining the
      *
      * @param HtmlHelper $html
      * @param JavascriptHelper $script
      * @param string $field_id 		DOM ID of field to observe
      * @param array $options 			ajax options
      * @return string 					ajax script
      */
	function autoComplete ($html, $script, $field, $url = "", $options = array())
	{
		if (!isset($options['id']))
		{
			$options['id'] = str_replace("/", "_", $field);
		}

		$htmlOptions = $options;
		$ajaxOptions = array('with', 'asynchronous', 'synchronous', 'method', 'position', 'form');
		foreach($ajaxOptions as $key)
		{
			if(isset($htmlOptions[$key]))
			{
				unset($htmlOptions[$key]);
			}
		}
		$htmlOptions['autocomplete'] = "off";

		foreach($ajaxOptions as $key)
		{
			if(isset($options[$key]))
			{
				$ajaxOptions[$key] = $options[$key];
			}
			else
			{
				unset($ajaxOptions[$key]);
			}
		}

		$divOptions = array('id' => $options['id'] . "_autoComplete", 'class' => "auto_complete");
		return $html->input($field, $htmlOptions) .
		$html->tag("div", $divOptions, true) . "</div>" .
		$script->codeBlock("new Ajax.Autocompleter('" . $options['id'] . "', '" . $divOptions['id'] . "', '" . $html->url($url) . "', " . $this->__optionsForAjax($ajaxOptions) . ");");
	}

	/**
     *
     */
	function drag($script, $id, $options = array())
	{
		return $script->codeBlock("new Draggable('$id', " . $this->__optionsForDraggable($options) . ");");
	}

	/**
      * For a reference on the options for this function, check out
      * http://wiki.script.aculo.us/scriptaculous/show/Droppables.add
      *
      */
	function drop($html, $script, $id, $options = array())
	{
		return $script->codeBlock("Droppables.add('$id', " . $this->__optionsForDroppable($options) . ");");
	}


	function dropRemote($html, $script, $id, $options = array(), $ajaxOptions = array())
	{
		$options['onDrop'] = "function(element){" . $this->remoteFunction($html, $ajaxOptions) . "}";
	}

	function __optionsForDraggable ($options)
	{
		$opts = array("handle" => "", "revert" => "true", "constraint" => "", "change" => "");
		foreach($opts as $key => $val)
		{
			if(isset($options[$key]))
			{
				$opts[$key] = $options[$key];
			}
			else
			{
				if($val == "") { unset($opts[$key]); }
			}
		}
		if(isset($opts['handle'])) { $opts['handle'] = "'" . $opts['handle'] . "'"; }
		if(isset($opts['constraint'])) { $opts['constraint'] = "'" . $opts['constraint'] . "'"; }

		$out = array();
		foreach ($opts as $k => $v)
		{
			$out[] = "$k:$v";
		}

		$out = join(', ', $out);
		$out = '{' . $out . '}';
		return $out;
	}


	function __optionsForDroppable ($options)
	{
		$opts = array("accept" => "", "containment" => "", "overlap" => "", "greedy" => "", "hoverclass" => "", "onHover" => "", "onDrop" => "");
		foreach($opts as $key => $val)
		{
			if(isset($options[$key]))
			{
				$opts[$key] = $options[$key];
			}
			else
			{
				if($val == "") { unset($opts[$key]); }
			}
		}
		if(isset($opts['accept'])) { $opts['accept'] = "'" . $opts['accept'] . "'"; }
		if(isset($opts['overlap'])) { $opts['overlap'] = "'" . $opts['overlap'] . "'"; }
		if(isset($opts['hoverclass'])) { $opts['hoverclass'] = "'" . $opts['hoverclass'] . "'"; }

		$out = array();
		foreach ($opts as $k => $v)
		{
			$out[] = "$k:$v";
		}

		$out = join(', ', $out);
		$out = '{' . $out . '}';
		return $out;
	}

	/**
      * Javascript helper function (private).
      *
      */
	function __optionsForAjax ($options)
	{
		$js_options = $this->__buildCallbacks($options);
		$js_options['asynchronous'] = 'true';
		if (isset($options['type']))
		{
			if ($options['type'] == 'synchronous')
			{
				$js_options['asynchronous'] = 'false';
			}
		}

		if (isset($options['method']))
		{
			$js_options['method'] = $this->__methodOptionToString($options['method']);
		}

		if (isset($options['position']))
		{
			$js_options['insertion'] = "Insertion." . Inflector::camelize($options['position']);
		}

		if (isset($options['form']))
		{
			$js_options['parameters'] = 'Form.serialize(this)';
		}
		elseif (isset($options['with']))
		{
			$js_options['parameters'] = $options['with'];
		}

		if (isset($options['evalScripts']))
		{
			$js_options['evalScripts'] = "true";
		}
		else
		{
			$js_options['evalScripts'] = "false";
		}

		$out = array();
		foreach ($js_options as $k => $v)
		{
			$out[] = "$k:$v";
		}

		$out = join(', ', $out);
		$out = '{' . $out . '}';
		return $out;
	}

	function __methodOptionToString ($method)
	{
		return (is_string($method) && !$method[0]=="'") ? $method : "'$method'";
	}

	function __buildObserver ($html, $klass, $name, $options=null)
	{
		if(!isset($options['with']) && isset($options['update']))
		{
			$options['with'] = 'value';
		}

		$callback = $this->remoteFunction($html, $options);
		$javascript = "new $klass('$name', ";
		$javascript .= (isset($options['frequency']) ? $options['frequency'] : 2) . ", function(element, value) {";
		$javascript .= "$callback})";
		return $javascript;
	}

	function __buildCallbacks($options)
	{
		$actions = array('uninitialized', 'loading', 'loaded', 'interactive', 'complete');
		$callbacks = array();
		foreach($actions as $callback)
		{
			if(isset($options[$callback]))
			{
				$name = 'on' . ucfirst($callback);
				$code = $options[$callback];
				$callbacks[$name] = "function(request){".$code."}";
			}
		}
		return $callbacks;
	}
}
?>