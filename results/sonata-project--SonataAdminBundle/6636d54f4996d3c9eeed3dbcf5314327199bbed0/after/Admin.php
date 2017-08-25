<?php

/*
 * This file is part of the Sonata package.
 *
 * (c) Thomas Rabaix <thomas.rabaix@sonata-project.org>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Sonata\BaseApplicationBundle\Admin;

use Symfony\Component\DependencyInjection\ContainerAware;
use Symfony\Component\Form\Form;

use Sonata\BaseApplicationBundle\Tool\Datagrid;

abstract class Admin extends ContainerAware
{
    protected $class;

    protected $listFields = false;

    protected $formFields = false;

    protected $filterFields = array(); // by default there is no filter

    protected $filterDatagrid;

    protected $maxPerPage = 25;

    protected $baseRouteName;

    protected $baseRoutePattern;

    protected $baseControllerName;

    protected $formGroups = false;

    /**
     *
     * @var array options to set to the form (ie, validation_groups)
     */
    protected $formOptions = array();

    // note : don't like this, but havn't find a better way to do it
    protected $configurationPool;

    protected $code;

    protected $label;

    protected $urls = array();

    protected $subject;

    /**
     * Reference the parent FieldDescription related to this admin
     * only set for FieldDescription which is associated to an Sub Admin instance
     *
     * FieldDescription
     */
    protected $parentFieldDescription;

    protected $loaded = array(
        'form_fields' => false,
        'form_groups' => false,
        'list_fields' => false,
        'urls'        => false,
    );

    protected $choicesCache = array();

    /**
     * return the entity manager
     *
     * @return EntityManager
     */
    abstract public function getEntityManager();

    /**
     * build the field to use in the list view
     *
     * @return void
     */
    abstract protected function buildListFields();

    abstract protected function getChoices(FieldDescription $description);

    public function getForm($object)
    {
        $form = $this->getBaseForm($object);

        $this->configureFormFields($form);

        return $form;
    }

    public function configure()
    {

    }

    /**
     * return the baseRoutePattern used to generate the routing information
     *
     * @throws RuntimeException
     * @return string the baseRoutePattern used to generate the routing information
     */
    public function getBaseRoutePattern()
    {

        if (!$this->baseRoutePattern) {
            if (preg_match('@(Application|Bundle)\\\([A-Za-z]*)\\\([A-Za-z]*)Bundle\\\(Entity|Document)\\\([A-Za-z]*)@', $this->getClass(), $matches)) {

                $this->baseRoutePattern = sprintf('/%s/%s/%s',
                    $this->urlize($matches[2], '-'),
                    $this->urlize($matches[3], '-'),
                    $this->urlize($matches[5], '-')
                );
            } else {
                throw new \RuntimeException(sprintf('Please define a default `baseRoutePattern` value for the admin class `%s`', get_class($this)));
            }
        }

        return $this->baseRoutePattern;
    }

    /**
     * return the baseRouteName used to generate the routing information
     *
     * @throws RuntimeException
     * @return string the baseRouteName used to generate the routing information
     */
    public function getBaseRouteName()
    {
        if (!$this->baseRouteName) {
            if (preg_match('@(Application|Bundle)\\\([A-Za-z]*)\\\([A-Za-z]*)Bundle\\\(Entity|Document)\\\([A-Za-z]*)@', $this->getClass(), $matches)) {

                $this->baseRouteName = sprintf('admin_%s_%s_%s',
                    $this->urlize($matches[2]),
                    $this->urlize($matches[3]),
                    $this->urlize($matches[5])
                );
            } else {

                throw new \RuntimeException(sprintf('Please define a default `baseRouteName` value for the admin class `%s`', get_class($this)));
            }
        }

        return $this->baseRouteName;
    }

    public function urlize($word, $sep = '_')
    {

        return strtolower(preg_replace('~(?<=\\w)([A-Z])~', $sep.'$1', $word));
    }

    /**
     * return the class name handled by the Admin instance
     *
     * @return string the class name handled by the Admin instance
     */
    public function getClass()
    {
        return $this->class;
    }

    /**
     * return the doctrine class metadata handled by the Admin instance
     *
     * @return ClassMetadataInfo the doctrine class metadata handled by the Admin instance
     */
    public function getClassMetaData()
    {

        return $this->getEntityManager()
            ->getClassMetaData($this->getClass());
    }

    /**
     * return the list of batchs actions
     *
     * @return array the list of batchs actions
     */
    public function getBatchActions()
    {

        return array(
            'delete' => 'action_delete'
        );
    }

    /**
     * return the list of available urls
     *
     * @return array the list of available urls
     */
    public function getUrls()
    {

        $this->buildUrls();

        return $this->urls;
    }

    public function buildUrls()
    {
        if ($this->loaded['urls']) {
            return;
        }

        $this->urls =  array(
            'list' => array(
                'name'      => $this->getBaseRouteName().'_list',
                'pattern'   => $this->getBaseRoutePattern().'/list',
                'defaults'  => array(
                    '_controller' => $this->getBaseControllerName().':list'
                ),
                'requirements' => array(),
                'options' => array(),
                'params'    => array(),
            ),
            'create' => array(
                'name'      => $this->getBaseRouteName().'_create',
                'pattern'       => $this->getBaseRoutePattern().'/create',
                'defaults'  => array(
                    '_controller' => $this->getBaseControllerName().':create'
                ),
                'requirements' => array(),
                'options' => array(),
                'params'    => array(),
            ),
            'edit' => array(
                'name'      => $this->getBaseRouteName().'_edit',
                'pattern'       => $this->getBaseRoutePattern().'/{id}/edit',
                'defaults'  => array(
                    '_controller' => $this->getBaseControllerName().':edit'
                ),
                'requirements' => array(),
                'options' => array(),
                'params'    => array(),
            ),
            'update' => array(
                'name'      => $this->getBaseRouteName().'_update',
                'pattern'       => $this->getBaseRoutePattern().'/update',
                'defaults'  => array(
                    '_controller' => $this->getBaseControllerName().':update'
                ),
                'requirements' => array(),
                'options' => array(),
                'params'    => array(),
            ),
            'batch' => array(
                'name'      => $this->getBaseRouteName().'_batch',
                'pattern'       => $this->getBaseRoutePattern().'/batch',
                'defaults'  => array(
                    '_controller' => $this->getBaseControllerName().':batch'
                ),
                'requirements' => array(),
                'options' => array(),
                'params'    => array(),
            )
        );

        $this->configureUrls();
    }

    public function configureUrls()
    {

    }

    /**
     * return the url defined by the $name
     *
     * @param  $name
     * @return bool
     */
    public function getUrl($name)
    {
        $urls = $this->getUrls();

        if (!isset($urls[$name])) {
            return false;
        }

        return $urls[$name];
    }

    /**
     * generate the url with the given $name
     *
     * @throws RuntimeException
     * @param  $name
     * @param array $params
     *
     * @return return a complete url
     */
    public function generateUrl($name, $params = array())
    {
        $url = $this->getUrl($name);

        if (!$url) {
            throw new \RuntimeException(sprintf('unable to find the url `%s`', $name));
        }

        if (!is_array($params)) {
            $params = array();
        }

        return $this->container->get('router')->generate($url['name'], array_merge($url['params'], $params));
    }

    /**
     * return the list template
     *
     * @return string the list template
     */
    public function getListTemplate()
    {
        return 'SonataBaseApplicationBundle:CRUD:list.twig.html';
    }

    /**
     * return the edit template
     *
     * @return string the edit template
     */
    public function getEditTemplate()
    {
        return 'SonataBaseApplicationBundle:CRUD:edit.twig.html';
    }

    public function getReflectionFields()
    {
        return $this->getClassMetaData()->reflFields;
    }

    public function getNewInstance()
    {
        $class = $this->getClass();

        return new $class;
    }

    /**
     *
     * @return Form the base form
     */
    public function getBaseForm($object)
    {
        // TODO $this->formOptions

        return $this->container->get('form.factory')->getForm('data', $object);
    }


    /**
     * attach an admin instance to the given FieldDescription
     *
     */
    public function attachAdminClass(FieldDescription $fieldDescription)
    {
        $pool = $this->getConfigurationPool();

        $admin = $pool->getAdminByClass($fieldDescription->getTargetEntity());
        if (!$admin) {
            throw new \RuntimeException(sprintf('You must define an Admin class for the `%s` field (targetEntity=%s)', $fieldDescription->getFieldName(), $fieldDescription->getTargetEntity()));
        }

        $fieldDescription->setAssociationAdmin($admin);
    }

    /**
     * return the target objet
     *
     * @param  $id
     * @return
     */
    public function getObject($id)
    {

        return $this->getEntityManager()
            ->find($this->getClass(), $id);
    }

    public function buildFormGroups(Form $form)
    {

        if ($this->loaded['form_groups']) {
            return;
        }

        $this->loaded['form_groups'] = true;


        if (!$this->formGroups) {
            $this->formGroups = array(
                false => array('fields' => array_keys($this->getFormFields($form)))
            );
        }

        // normalize array
        foreach ($this->formGroups as $name => $group) {
            if (!isset($this->formGroups[$name]['collapsed'])) {
                $this->formGroups[$name]['collapsed'] = false;
            }
        }
    }

    public function preUpdate($object)
    {

    }

    public function postUpdate($object)
    {

    }

    public function preInsert($object)
    {

    }

    public function postInsert($object)
    {

    }

    public function configureListFields()
    {

    }

    public function configureFilterFields()
    {

    }

    abstract protected function configureFormFields(Form $form);

    public function getFilterDatagrid()
    {
        if (!$this->filterDatagrid) {

            $this->filterDatagrid = new Datagrid(
                $this->getClass(),
                $this->getEntityManager()
            );

            $this->filterDatagrid->setMaxPerPage($this->maxPerPage);

            // first pass, configure and normalize the filterFields array
            $this->filterDatagrid->setFilterFields($this->filterFields);
            $this->filterDatagrid->buildFilterFields();

            // update the current filterFields array and apply admin custom code
            $this->filterFields = $this->filterDatagrid->getFilterFields();
            $this->configureFilterFields();

            // set the final value to the datagrid
            $this->filterDatagrid->setFilterFields($this->filterFields);

        }

        return $this->filterDatagrid;
    }

    /**
     *
     */
    public function getRootCode()
    {
        return $this->getRoot()->getCode();
    }

    /**
     * return the master admin
     *
     */
    public function getRoot()
    {

        $parentFieldDescription = $this->getParentFieldDescription();

        if (!$parentFieldDescription) {

            return $this;
        }

        return $parentFieldDescription->getAdmin()->getRoot();
    }

    /**
     * Construct and build the form field definitions
     *
     * @return list form field definition
     */
    public function getFormFields(Form $form)
    {
        $fields = array();

        foreach ($form as $fieldName => $field) {
            $fields[$fieldName] = new FieldDescription();
            $fields[$fieldName]->setFieldName($fieldName);
            $fields[$fieldName]->setAdmin($this);
            $fields[$fieldName]->setTemplate('SonataBaseApplicationBundle:CRUD:edit_text.twig.html');

            // TODO: set description properties
        }

        /*
        $this->formFields = self::getBaseFields($form);

        foreach ($this->formFields as $name => $fieldDescription) {

            if (!$fieldDescription->getType()) {
                throw new \RuntimeException(sprintf('You must declare a type for the field `%s`', $name));
            }

            $fieldDescription->setAdmin($this);
            $fieldDescription->setOption('edit', $fieldDescription->getOption('edit', 'standard'));

            // fix template value for doctrine association fields
            if (!$fieldDescription->getTemplate()) {

                $fieldDescription->setTemplate(sprintf('SonataBaseApplicationBundle:CRUD:edit_%s.twig.html', $fieldDescription->getType()));

                if ($fieldDescription->getType() == ClassMetadataInfo::ONE_TO_ONE) {
                    $fieldDescription->setTemplate('SonataBaseApplicationBundle:CRUD:edit_one_to_one.twig.html');
                    $this->attachAdminClass($fieldDescription);
                }

                if ($fieldDescription->getType() == ClassMetadataInfo::MANY_TO_ONE) {
                    $fieldDescription->setTemplate('SonataBaseApplicationBundle:CRUD:edit_many_to_one.twig.html');
                    $this->attachAdminClass($fieldDescription);
                }

                if ($fieldDescription->getType() == ClassMetadataInfo::MANY_TO_MANY) {
                    $fieldDescription->setTemplate('SonataBaseApplicationBundle:CRUD:edit_many_to_many.twig.html');
                    $this->attachAdminClass($fieldDescription);
                }

                if ($fieldDescription->getType() == ClassMetadataInfo::ONE_TO_MANY) {
                    $fieldDescription->setTemplate('SonataBaseApplicationBundle:CRUD:edit_one_to_many.twig.html');

                    if($fieldDescription->getOption('edit') == 'inline' && !$fieldDescription->getOption('widget')) {
                        $fieldDescription->setOption('widget', 'Bundle\\Sonata\\BaseApplicationBundle\\Form\\EditableGroupField');
                    }

                    $this->attachAdminClass($fieldDescription);
                }
            }

            // set correct default value
            if ($fieldDescription->getType() == 'datetime') {
                $options = $fieldDescription->getOption('form_fields', array());
                if (!isset($options['years'])) {
                    $options['years'] = range(1900, 2100);
                }
                $fieldDescription->setOption('form_field', $options);
            }

            // unset the identifier field as it is not required to update an object
            if ($fieldDescription->isIdentifier()) {
                unset($this->formFields[$name]);
            }
        */

        return $fields;
    }

    public function getListFields()
    {
        $this->buildListFields();

        return $this->listFields;
    }

    public function setBaseControllerName($baseControllerName)
    {
        $this->baseControllerName = $baseControllerName;
    }

    public function getBaseControllerName()
    {
        return $this->baseControllerName;
    }

    public function setConfigurationPool($configurationPool)
    {
        $this->configurationPool = $configurationPool;
    }

    public function getConfigurationPool()
    {
        return $this->configurationPool;
    }

    public function setCode($code)
    {
        $this->code = $code;
    }

    public function getCode()
    {
        return $this->code;
    }

    public function setLabel($label)
    {
        $this->label = $label;
    }

    public function getLabel()
    {
        return $this->label;
    }

    public function setFilterFields($filterFields)
    {
        $this->filterFields = $filterFields;
    }

    public function getFilterFields()
    {
        return $this->filterFields;
    }

    public function setMaxPerPage($maxPerPage)
    {
        $this->maxPerPage = $maxPerPage;
    }

    public function getMaxPerPage()
    {
        return $this->maxPerPage;
    }

    public function setFormGroups($formGroups)
    {
        $this->formGroups = $formGroups;
    }

    public function getFormGroups(Form $form)
    {
        $this->buildFormGroups($form);

        return $this->formGroups;
    }

    public function setParentFieldDescription($parentFieldDescription)
    {
        $this->parentFieldDescription = $parentFieldDescription;
    }

    public function getParentFieldDescription()
    {
        return $this->parentFieldDescription;
    }

    public function setSubject($subject)
    {
        $this->subject = $subject;
    }

    public function getSubject()
    {
        return $this->subject;
    }
}