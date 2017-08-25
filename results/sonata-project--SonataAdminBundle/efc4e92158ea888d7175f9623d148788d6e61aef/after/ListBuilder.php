<?php

/*
 * This file is part of the Sonata package.
 *
 * (c) Thomas Rabaix <thomas.rabaix@sonata-project.org>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Sonata\BaseApplicationBundle\Builder;

use Sonata\BaseApplicationBundle\Admin\FieldDescription;
use Sonata\BaseApplicationBundle\Admin\Admin;
use Sonata\BaseApplicationBundle\Datagrid\ListCollection;

use Doctrine\ORM\Mapping\ClassMetadataInfo;

class ListBuilder implements ListBuilderInterface
{

    public function getBaseList(array $options = array())
    {

        return new ListCollection;
    }

    public function addField(ListCollection $list, FieldDescription $fieldDescription)
    {

        return $list->add($fieldDescription);
    }

    /**
     * The method define the correct default settings for the provided FieldDescription
     *
     * @param FieldDescription $fieldDescription
     * @return void
     */
    public function fixFieldDescription(Admin $admin, FieldDescription $fieldDescription, array $options = array())
    {

        $fieldDescription->mergeOptions($options);
        $fieldDescription->setAdmin($admin);

        // set the default field mapping
        if (isset($admin->getClassMetaData()->fieldMappings[$fieldDescription->getName()])) {
            $fieldDescription->setFieldMapping($admin->getClassMetaData()->fieldMappings[$fieldDescription->getName()]);
        }

        // set the default association mapping
        if (isset($admin->getClassMetaData()->associationMappings[$fieldDescription->getName()])) {
            $fieldDescription->setAssociationMapping($admin->getClassMetaData()->associationMappings[$fieldDescription->getName()]);
        }

        $fieldDescription->setOption('code', $fieldDescription->getOption('code', $fieldDescription->getName()));
        $fieldDescription->setOption('label', $fieldDescription->getOption('label', $fieldDescription->getName()));

        if (!$fieldDescription->getTemplate()) {

            $fieldDescription->setTemplate(sprintf('SonataBaseApplicationBundle:CRUD:list_%s.twig.html', $fieldDescription->getType()));

            if ($fieldDescription->getType() == ClassMetadataInfo::MANY_TO_ONE) {
                $fieldDescription->setTemplate('SonataBaseApplicationBundle:CRUD:list_many_to_one.twig.html');
            }

            if ($fieldDescription->getType() == ClassMetadataInfo::ONE_TO_ONE) {
                $fieldDescription->setTemplate('SonataBaseApplicationBundle:CRUD:list_one_to_one.twig.html');
            }

            if ($fieldDescription->getType() == ClassMetadataInfo::ONE_TO_MANY) {
                $fieldDescription->setTemplate('SonataBaseApplicationBundle:CRUD:list_one_to_many.twig.html');
            }

            if ($fieldDescription->getType() == ClassMetadataInfo::MANY_TO_MANY) {
                $fieldDescription->setTemplate('SonataBaseApplicationBundle:CRUD:list_many_to_many.twig.html');
            }
        }

        if ($fieldDescription->getType() == ClassMetadataInfo::MANY_TO_ONE) {
            $admin->attachAdminClass($fieldDescription);
        }

        if ($fieldDescription->getType() == ClassMetadataInfo::ONE_TO_ONE) {
            $admin->attachAdminClass($fieldDescription);
        }

        if ($fieldDescription->getType() == ClassMetadataInfo::ONE_TO_MANY) {
            $admin->attachAdminClass($fieldDescription);
        }

        if ($fieldDescription->getType() == ClassMetadataInfo::MANY_TO_MANY) {
            $admin->attachAdminClass($fieldDescription);
        }
    }
}