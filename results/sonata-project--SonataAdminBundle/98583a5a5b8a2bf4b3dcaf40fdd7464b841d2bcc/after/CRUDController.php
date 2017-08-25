<?php

/*
 * This file is part of the Sonata package.
 *
 * (c) Thomas Rabaix <thomas.rabaix@sonata-project.org>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Bundle\BaseApplicationBundle\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\Controller;
use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;

use Symfony\Component\Form\Form;


use Bundle\BaseApplicationBundle\Tool\DoctrinePager as Pager;

class CRUDController extends Controller
{
    protected $class;

    protected $list_fields = false;

    protected $form_fields = false;

    protected $base_route = '';

    protected $base_controller_name;

    /**
     * Sets the Container associated with this Controller.
     *
     * @param ContainerInterface $container A ContainerInterface instance
     */
    public function setContainer(\Symfony\Component\DependencyInjection\ContainerInterface $container = null)
    {
        $this->container = $container;

        $this->configure();
    }

    public function configure()
    {
        $this->buildFormFields();
        $this->buildListFields();
    }

    public function getClass()
    {
        return $this->class;
    }

    public function getEntityManager()
    {
        return $this->get('doctrine.orm.default_entity_manager');
    }

    public function getClassMetaData()
    {
        $em             = $this->getEntityManager();

        return $em->getClassMetaData($this->getClass());
    }

    public function getListQueryBuilder()
    {
        $em             = $this->getEntityManager();
        $repository     = $em->getRepository($this->getClass());

        $query_buidler = $repository
            ->createQueryBuilder('c');

        return $query_buidler;
    }

    public function getBatchActions()
    {

        return array(
            'delete' => 'Delete'
        );
    }

    public function getUrls()
    {
        return array(
            'list' => array(
                'url'       => $this->base_route.'_list',
                'params'    => array(),
            ),
            'create' => array(
                'url'       => $this->base_route.'_create',
                'params'    => array(),
            ),
            'update' => array(
                'url'       => $this->base_route.'_update',
                'params'    => array()
            ),
            'delete' => array(
                'url'       => $this->base_route.'_delete',
                'params'    => array()
            ),
            'edit'   => array(
                'url'       => $this->base_route.'_edit',
                'params'    => array()
            ),
            'batch'   => array(
                'url'       => $this->base_route.'_batch',
                'params'    => array()
            )
        );
    }

    public function getUrl($name)
    {
        $urls = $this->getUrls();

        if(!isset($urls[$name])) {
            return false;
        }

        return $urls[$name];
    }

    public function listAction()
    {

        $pager = new Pager($this->getClass());

        $url = $this->getUrl('list');

        $pager->setRouter($this->get('router'));
        $pager->setRoute($url['url']);

        $pager->setQueryBuilder($this->getListQueryBuilder());
        $pager->setPage($this->get('request')->get('page', 1));
        $pager->init();

        return $this->render($this->getListTemplate(), array(
            'pager'             => $pager,
            'fields'            => $this->getListFields(),
            'class_meta_data'   => $this->getClassMetaData(),
            'urls'              => $this->getUrls(),
            'batch_actions'     => $this->getBatchActions(),
        ));

    }

    public function getListTemplate()
    {
        return 'BaseApplicationBundle:CRUD:list.twig';
    }

    public function getEditTemplate()
    {
        return 'BaseApplicationBundle:CRUD:edit.twig';
    }

    public function getReflectionFields()
    {
        return $this->getClassMetaData()->reflFields;
    }

    /**
     * make sure the base field are set in the correct format
     *
     * @param  $selected_fields
     * @return array
     */
    public function getBaseFields($selected_fields)
    {
        // if nothing is defined we display all fields
        if(!$selected_fields) {
            $selected_fields = array_keys($this->getClassMetaData()->reflFields) + array_keys($this->getClassMetaData()->associationMappings);
        }

        $metadata = $this->getClassMetaData();

        // make sure we works with array
        foreach($selected_fields as $name => $options) {
            if(is_array($options)) {
                $fields[$name] = $options;
            } else {
                $fields[$options] = array();
                $name = $options;
            }

            if(isset($metadata->fieldMappings[$name])) {
                $fields[$name] = array_merge(
                    $metadata->fieldMappings[$name],
                    $fields[$name]
                );
            }


            if(isset($metadata->associationMappings[$name])) {
                $fields[$name] = array_merge(
                    $metadata->associationMappings[$name],
                    $fields[$name]
                );
            }

            if(isset($metadata->reflFields[$name])) {
                $fields[$name]['reflection']  =& $metadata->reflFields[$name];
            }
        }

        return $fields;
    }

    /**
     * @return void
     */
    public function configureFormFields()
    {

    }

    public function buildFormFields()
    {
        $this->form_fields = $this->getBaseFields($this->form_fields);

        foreach($this->form_fields as $name => $options) {

            if(!isset($this->form_fields[$name]['options'])) {
                $this->form_fields[$name]['options'] = array();
            }

            if(!isset($this->form_fields[$name]['template']) && isset($this->form_fields[$name]['type'])) {
                $this->form_fields[$name]['template'] = sprintf('BaseApplicationBundle:CRUD:edit_%s.twig', $this->form_fields[$name]['type']);

                if($this->form_fields[$name]['type'] == \Doctrine\ORM\Mapping\ClassMetadataInfo::ONE_TO_ONE)
                {
                    $this->form_fields[$name]['template'] = 'BaseApplicationBundle:CRUD:edit_one_to_one.twig';
                }

                if($this->form_fields[$name]['type'] == \Doctrine\ORM\Mapping\ClassMetadataInfo::MANY_TO_MANY)
                {
                    $this->form_fields[$name]['template'] = 'BaseApplicationBundle:CRUD:edit_many_to_many.twig';
                }
            }

            if(isset($this->form_fields[$name]['id'])) {
                unset($this->form_fields[$name]);
            }
        }

        $this->configureFormFields();

        return $this->form_fields;
    }

    public function buildListFields()
    {
        $this->list_fields = $this->getBaseFields($this->list_fields);

        foreach($this->list_fields as $name => $options) {
            if(!isset($this->list_fields[$name]['type'])) {
                $this->list_fields[$name]['type'] = 'string';
            }

            if(!isset($this->list_fields[$name]['template'])) {
                $this->list_fields[$name]['template'] = sprintf('BaseApplicationBundle:CRUD:list_%s.twig', $this->list_fields[$name]['type']);
            }

            if(isset($this->list_fields[$name]['id'])) {
                $this->list_fields[$name]['template'] = 'BaseApplicationBundle:CRUD:list_identifier.twig';
            }
        }

        if(!isset($this->list_fields['_batch'])) {
            $this->list_fields = array('_batch' => array(
                'template' => 'BaseApplicationBundle:CRUD:list__batch.twig'
            ) ) + $this->list_fields;
        }

    }

    /**
     * Construct and build the form field definitions
     *
     * @return list form field definition
     */
    public function getFormFields()
    {
        return $this->form_fields;
    }

    public function getListFields()
    {
        return $this->list_fields;
    }

    public function batchActionDelete($idx)
    {
        $em = $this->getEntityManager();

        $query_builder = $em->createQueryBuilder();
        $objects = $query_builder
            ->select('o')
            ->from($this->getClass(), 'o')
            ->add('where', $query_builder->expr()->in('o.id', $idx))
            ->getQuery()
            ->execute();

        foreach($objects as $object) {
            $em->remove($object);
        }

        $em->flush();

        // todo : add confirmation flash var
        $url = $this->getUrl('list');
        return $this->redirect($this->generateUrl($url['url']));
    }

    public function deleteAction($id)
    {

    }

    public function editAction($id)
    {

        $this->get('session')->start();

        $fields = $this->getFormFields();

        if($id instanceof Form) {
            $object = $id->getData();
            $form   = $id;
        } else {
            $object = $this->get('doctrine.orm.default_entity_manager')->find($this->getClass(), $id);

            if(!$object) {
                throw new NotFoundHttpException(sprintf('unable to find the object with id : %s', $id));
            }

            $form   = $this->getForm($object, $fields);
        }

        return $this->render($this->getEditTemplate(), array(
            'form'   => $form,
            'object' => $object,
            'fields' => $fields,
            'urls'   => $this->getUrls()
        ));
    }

    public function getChoices($description)
    {
        $targets = $this->getEntityManager()
            ->createQueryBuilder()
            ->select('t')
            ->from($description['targetEntity'], 't')
            ->getQuery()
            ->execute();

        $choices = array();
        foreach($targets as $target) {
            // todo : puts this into a configuration option and use reflection
            foreach(array('getTitle', 'getName', '__toString') as $getter) {
                if(method_exists($target, $getter)) {
                    $choices[$target->getId()] = $target->$getter();
                    break;
                }
            }
        }

        return $choices;
    }

    public function getForm($object, $fields)
    {

        $form = new Form('data', $object, $this->get('validator'));

        foreach($fields as $name => $description) {

            if(!isset($description['type'])) {

                continue;
            }

            switch($description['type']) {

                case \Doctrine\ORM\Mapping\ClassMetadataInfo::MANY_TO_MANY:

                    $transformer = new \Symfony\Bundle\DoctrineBundle\Form\ValueTransformer\CollectionToChoiceTransformer(array(
                        'em'        =>  $this->getEntityManager(),
                        'className' => $description['targetEntity']
                    ));

                    $field = new \Symfony\Component\Form\ChoiceField($name, array_merge(array(
                        'expanded' => true,
                        'multiple' => true,
                        'choices' => $this->getChoices($description),
                        'value_transformer' => $transformer,
                    ), $description['options']));

                    break;

                case \Doctrine\ORM\Mapping\ClassMetadataInfo::ONE_TO_ONE:

                    $transformer = new \Symfony\Bundle\DoctrineBundle\Form\ValueTransformer\EntityToIDTransformer(array(
                        'em'        =>  $this->getEntityManager(),
                        'className' => $description['targetEntity']
                    ));

                    $field = new \Symfony\Component\Form\ChoiceField($name, array_merge(array(
                        'expanded' => false,
                        'choices' => $this->getChoices($description),
                        'value_transformer' => $transformer,
                    ), $description['options']));

                    break;

                case 'string':
                    $field = new \Symfony\Component\Form\TextField($name, $description['options']);
                    break;

                case 'text':
                    $field = new \Symfony\Component\Form\TextareaField($name, $description['options']);
                    break;

                case 'boolean':
                    $field = new \Symfony\Component\Form\CheckboxField($name, $description['options']);
                    break;

                case 'integer':
                    $field = new \Symfony\Component\Form\IntegerField($name, $description['options']);
                    break;

                case 'decimal':
                    $field = new \Symfony\Component\Form\NumberField($name, $description['options']);
                    break;

                case 'datetime':
                    $field = new \Symfony\Component\Form\DateTimeField($name, $description['options']);
                    break;

                case 'date':
                    $field = new \Symfony\Component\Form\DateField($name, $description['options']);
                    break;

                case 'choice':
                    $field = new \Symfony\Component\Form\ChoiceField($name, $description['options']);
                    break;

                case 'array':
                    $field = new \Symfony\Component\Form\FieldGroup($name, $description['options']);

                    $values = $description['reflection']->getValue($object);

                    foreach((array)$values as $k => $v) {
                        $field->add(new \Symfony\Component\Form\TextField($k));
                    }
                    break;

                default:
                    throw new \RuntimeException(sprintf('unknow type `%s`', $description['type']));
            }

            $form->add($field);

        }

        return $form;
    }

    public function updateAction()
    {

        $this->get('session')->start();

        if($this->get('request')->getMethod() != 'POST') {
           throw new \RuntimeException('invalid request type, POST expected');
        }

        $id = $this->get('request')->get('id');

        if(is_numeric($id)) {
            $object = $this->get('doctrine.orm.default_entity_manager')->find($this->getClass(), $id);

            if(!$object) {
                throw new NotFoundHttpException(sprintf('unable to find the object with id : %s', $id));
            }

            $action = 'edit';
        } else {
            $class = $this->getClass();
            $object = new $class;

            $action = 'create';
        }

        $fields = $this->getFormFields();
        $form   = $this->getForm($object, $fields);

        $form->bind($this->get('request')->get('data'));

        if($form->isValid()) {

            $this->getEntityManager()->persist($object);
            $this->getEntityManager()->flush($object);

            // redirect to edit mode
            $url = $this->getUrl('edit');

            return $this->redirect($this->generateUrl($url['url'], array('id' => $object->getId())));
        }

        return $this->forward(sprintf('%s:%s', $this->getBaseControllerName(), $action), array(
            'id' => $form
        ));
    }

    public function batchAction()
    {
        if($this->get('request')->getMethod() != 'POST') {
           throw new \RuntimeException('invalid request type, POST expected');
        }

        $action = $this->get('request')->get('action');
        $idx    = $this->get('request')->get('idx');

        if(count($idx) == 0) { // no item selected
            // todo : add flash information

            $url = $this->getUrl('list');
            return $this->redirect($this->generateUrl($url['url']));
        }

        // execute the action, batchActionXxxxx
        $final_action = sprintf('batchAction%s', ucfirst($action));
        if(!method_exists($this, $final_action)) {
            throw new \RuntimeException(sprintf('A %s::%s method must be created', get_class($this), $final_action));
        }

        return call_user_func(array($this, $final_action), $idx);
    }

    public function createAction($form = null)
    {
        $this->get('session')->start();

        $fields = $this->getFormFields();

        if($form instanceof Form) {
            $object = $form->getData();
        } else {
            $class = $this->getClass();
            $object = new $class;

            $form   = $this->getForm($object, $fields);
        }

        return $this->render($this->getEditTemplate(), array(
            'form'   => $form,
            'object' => $object,
            'fields' => $fields,
            'urls'   => $this->getUrls()
        ));
    }

    public function setBaseControllerName($base_controller_name)
    {
        $this->base_controller_name = $base_controller_name;
    }

    public function getBaseControllerName()
    {
        return $this->base_controller_name;
    }

    public function setBaseRoute($base_route)
    {
        $this->base_route = $base_route;
    }

    public function getBaseRoute()
    {
        return $this->base_route;
    }
}