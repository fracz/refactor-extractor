<?php
/**
 * CakePHP(tm) : Rapid Development Framework (http://cakephp.org)
 * Copyright (c) Cake Software Foundation, Inc. (http://cakefoundation.org)
 *
 * Licensed under The MIT License
 * For full copyright and license information, please see the LICENSE.txt
 * Redistributions of files must retain the above copyright notice.
 *
 * @copyright     Copyright (c) Cake Software Foundation, Inc. (http://cakefoundation.org)
 * @link          http://cakephp.org CakePHP(tm) Project
 * @since         3.3.6
 * @license       http://www.opensource.org/licenses/mit-license.php MIT License
 */
namespace Cake\View\Helper;

use Cake\View\Helper;
use Cake\View\StringTemplateTrait;
use LogicException;

/**
 * BreadcrumbsHelper to register and display a breadcrumb trail for your views
 *
 * @property \Cake\View\Helper\UrlHelper $Url
 */
class BreadcrumbsHelper extends Helper
{

    use StringTemplateTrait;

    /**
     * Other helpers used by BreadcrumbsHelper
     *
     * @var array
     */
    public $helpers = ['Url'];

    /**
     * Default config for the helper.
     *
     * @var array
     */
    protected $_defaultConfig = [
        'templates' => [
            'wrapper' => '<ul{{attrs}}>{{content}}</ul>',
            'item' => '<li{{attrs}}><a href="{{link}}"{{innerAttrs}}>{{title}}</a></li>{{separator}}',
            'itemWithoutLink' => '<li{{attrs}}><span{{innerAttrs}}>{{title}}</span></li>{{separator}}',
            'separator' => '<li{{attrs}}><span{{innerAttrs}}>{{custom}}{{separator}}</span></li>'
        ]
    ];

    /**
     * The crumbs list.
     *
     * @var array
     */
    protected $crumbs = [];

    /**
     * Add a crumb to the trail.
     *
     * @param string|array $title Title of the crumb. If provided as an array, it will add each values of the array
     * as a single crumb. This allows you to add multiple crumbs in the trail at once. Arrays are expected to be of this
     * form:
     * - *title* The title of the crumb
     * - *link* The link of the crumb. If not provided, no link will be made
     * - *options* Options of the crumb. See description of params option of this method.
     * @param string|array|null $link Link of the crumb. Either a string, an array of route params to pass to
     * Url::build() or null / empty if the crumb does not have a link
     * @param array $options Array of options. These options will be used as attributes HTML attribute the crumb will
     * be rendered in (a <li> tag by default). It accepts two special keys:
     * - *innerAttrs*: An array that allows you to define attributes for the inner element of the crumb (by default, to
     * the link)
     * - *templateVars*: Specific template vars in case you override the templates provided
     * @return $this
     */
    public function add($title, $link = null, array $options = [])
    {
        if (is_array($title)) {
            foreach ($title as $crumb) {
                $crumb = array_merge(['title' => '', 'link' => null, 'options' => []], $crumb);
                $this->crumbs[] = $crumb;
            }

            return $this;
        }

        $this->crumbs[] = ['title' => $title, 'link' => $link, 'options' => $options];

        return $this;
    }

    /**
     * Prepend a crumb to the start of the queue.
     *
     * @param string $title Title of the crumb
     * @param string|array|null $link Link of the crumb. Either a string, an array of route params to pass to
     * Url::build() or null / empty if the crumb does not have a link
     * @param array $options Array of options. These options will be used as attributes HTML attribute the crumb will
     * be rendered in (a <li> tag by default). It accepts two special keys:
     * - *innerAttrs*: An array that allows you to define attributes for the inner element of the crumb (by default, to
     * the link)
     * - *templateVars*: Specific template vars in case you override the templates provided
     * @return $this
     */
    public function prepend($title, $link = null, array $options = [])
    {
        array_unshift($this->crumbs, ['title' => $title, 'link' => $link, 'options' => $options]);

        return $this;
    }

    /**
     * Insert a crumb at a specific index.
     *
     * If the index already exists, the new crumb will be inserted,
     * and the existing element will be shifted one index greater.
     *
     * @param int $index The index to insert at.
     * @param string $title Title of the crumb
     * @param string|array|null $link Link of the crumb. Either a string, an array of route params to pass to
     * Url::build() or null / empty if the crumb does not have a link
     * @param array $options Array of options. These options will be used as attributes HTML attribute the crumb will
     * be rendered in (a <li> tag by default). It accepts two special keys:
     * - *innerAttrs*: An array that allows you to define attributes for the inner element of the crumb (by default, to
     * the link)
     * - *templateVars*: Specific template vars in case you override the templates provided
     * @return $this
     */
    public function insertAt($index, $title, $link = null, array $options = [])
    {
        array_splice($this->crumbs, $index, 0, [['title' => $title, 'link' => $link, 'options' => $options]]);

        return $this;
    }

    /**
     * Insert a crumb before the first matching crumb with the specified title.
     *
     * Finds the index of the first middleware that matches the provided class,
     * and inserts the supplied callable before it.
     *
     * @param string $matchingTitle The title of the crumb you want to insert this one before
     * @param string $title Title of the crumb
     * @param string|array|null $link Link of the crumb. Either a string, an array of route params to pass to
     * Url::build() or null / empty if the crumb does not have a link
     * @param array $options Array of options. These options will be used as attributes HTML attribute the crumb will
     * be rendered in (a <li> tag by default). It accepts two special keys:
     * - *innerAttrs*: An array that allows you to define attributes for the inner element of the crumb (by default, to
     * the link)
     * - *templateVars*: Specific template vars in case you override the templates provided
     * @return $this
     */
    public function insertBefore($matchingTitle, $title, $link = null, array $options = [])
    {
        $key = $this->findCrumb($matchingTitle);

        if ($key !== null) {
            return $this->insertAt($key, $title, $link, $options);
        }
        throw new LogicException(sprintf("No crumb matching '%s' could be found.", $matchingTitle));
    }

    /**
     * Insert a crumb after the first matching crumb with the specified title.
     *
     * Finds the index of the first middleware that matches the provided class,
     * and inserts the supplied callable before it.
     *
     * @param string $matchingTitle The title of the crumb you want to insert this one after
     * @param string $title Title of the crumb
     * @param string|array|null $link Link of the crumb. Either a string, an array of route params to pass to
     * Url::build() or null / empty if the crumb does not have a link
     * @param array $options Array of options. These options will be used as attributes HTML attribute the crumb will
     * be rendered in (a <li> tag by default). It accepts two special keys:
     * - *innerAttrs*: An array that allows you to define attributes for the inner element of the crumb (by default, to
     * the link)
     * - *templateVars*: Specific template vars in case you override the templates provided
     * @return $this
     */
    public function insertAfter($matchingTitle, $title, $link = null, array $options = [])
    {
        $key = $this->findCrumb($matchingTitle);

        if ($key !== null) {
            return $this->insertAt($key + 1, $title, $link, $options);
        }
        throw new LogicException(sprintf("No crumb matching '%s' could be found.", $matchingTitle));
    }

    /**
     * Returns the crumbs list
     *
     * @return array
     */
    public function getCrumbs()
    {
        return $this->crumbs;
    }

    /**
     * Renders the breadcrumbs trail
     *
     * @param array $attributes Array of attributes applied to the `wrapper` template. Accepts the `templateVars` key to
     * allow the insertion of custom template variable in the template.
     * @param array $separator Array of attributes for the `separator` template.
     * Possible properties are :
     * - *separator* The string to be displayed as a separator
     * - *templateVars* Allows the insertion of custom template variable in the template
     * - *innerAttrs* To provide attributes in case your separator is divided in two elements
     * All other properties will be converted as HTML attributes and will replace the *attrs* key in the template
     * If you use the default for this option (empty), it will not render a separator.
     * @return string The breadcrumbs trail
     */
    public function render(array $attributes = [], array $separator = [])
    {
        $crumbs = $this->crumbs;
        $crumbsCount = count($crumbs);
        $templater = $this->templater();

        $separatorParams = [];
        if ($separator) {
            if (isset($separator['innerAttrs'])) {
                $separatorParams['innerAttrs'] = $templater->formatAttributes($separator['innerAttrs']);
                unset($separator['innerAttrs']);
            }

            if (isset($separator['separator'])) {
                $separatorParams['separator'] = $separator['separator'];
                unset($separator['separator']);
            }

            if (isset($separator['templateVars'])) {
                $separatorParams['templateVars'] = $separator['templateVars'];
                unset($separator['templateVars']);
            }

            $separatorParams['attrs'] = $templater->formatAttributes($separator);
        }

        $crumbTrail = '';
        foreach ($crumbs as $key => $crumb) {
            $link = $this->prepareLink($crumb['link']);
            $title = $crumb['title'];
            $options = $crumb['options'];

            $optionsLink = [];
            if (isset($options['innerAttrs'])) {
                $optionsLink = $options['innerAttrs'];
                unset($options['innerAttrs']);
            }

            $template = 'item';
            $templateParams = [
                'attrs' => $templater->formatAttributes($options, ['templateVars']),
                'innerAttrs' => $templater->formatAttributes($optionsLink),
                'title' => $title,
                'link' => $link,
                'separator' => '',
                'templateVars' => isset($options['templateVars']) ? $options['templateVars'] : []
            ];

            if (!($link)) {
                $template = 'itemWithoutLink';
            }

            if ($separatorParams && $key !== ($crumbsCount - 1)) {
                $templateParams['separator'] = $this->formatTemplate('separator', $separatorParams);
            }

            $crumbTrail .= $this->formatTemplate($template, $templateParams);
        }

        $crumbTrail = $this->formatTemplate('wrapper', [
            'content' => $crumbTrail,
            'attrs' => $templater->formatAttributes($attributes, ['templateVars']),
            'templateVars' => isset($attributes['templateVars']) ? $attributes['templateVars'] : []
        ]);

        return $crumbTrail;
    }

    /**
     * Search a crumb in the current stack which title matches the one provided as argument.
     * If found, the index of the matching crumb will be returned.
     *
     * @param string $title Title to find
     * @return int|null Index of the crumb found, or null if it can not be found
     */
    protected function findCrumb($title)
    {
        foreach ($this->crumbs as $key => $crumb) {
            if ($crumb['title'] === $title) {
                return $key;
            }
        }

        return null;
    }

    /**
     * Prepare the URL for a specific `link` param of a crumb
     *
     * @param array|string|null $link If array, an array of Router url params
     * If string, will be used as is
     * If empty, will consider that there is no link
     *
     * @return null|string The URL of a crumb
     */
    protected function prepareLink($link = null)
    {
        if (!$link) {
            return null;
        }

        return $this->Url->build($link);
    }
}