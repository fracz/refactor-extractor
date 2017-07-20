<?php
/*
* 2007-2011 PrestaShop
*
* NOTICE OF LICENSE
*
* This source file is subject to the Open Software License (OSL 3.0)
* that is bundled with this package in the file LICENSE.txt.
* It is also available through the world-wide-web at this URL:
* http://opensource.org/licenses/osl-3.0.php
* If you did not receive a copy of the license and are unable to
* obtain it through the world-wide-web, please send an email
* to license@prestashop.com so we can send you a copy immediately.
*
* DISCLAIMER
*
* Do not edit or add to this file if you wish to upgrade PrestaShop to newer
* versions in the future. If you wish to customize PrestaShop for your
* needs please refer to http://www.prestashop.com for more information.
*
*  @author PrestaShop SA <contact@prestashop.com>
*  @copyright  2007-2011 PrestaShop SA
*  @version  Release: $Revision$
*  @license    http://opensource.org/licenses/osl-3.0.php  Open Software License (OSL 3.0)
*  International Registered Trademark & Property of PrestaShop SA
*/

class AdminImagesController extends AdminController
{
	private $start_time = 0;
	private $max_execution_time = 7200;

	public function __construct()
	{
		$this->table = 'image_type';
		$this->className = 'ImageType';
		$this->lang = false;

	 	$this->addRowAction('edit');
		$this->addRowAction('delete');

		$this->fieldsDisplay = array(
			'id_image_type' => array('title' => $this->l('ID'), 'align' => 'center', 'width' => 25),
			'name' => array('title' => $this->l('Name'), 'width' => 140, 'size' => 16),
			'width' => array('title' => $this->l('Width'), 'align' => 'right', 'suffix' => ' px', 'width' => 50, 'size' => 5),
			'height' => array('title' => $this->l('Height'), 'align' => 'right', 'suffix' => ' px', 'width' => 50, 'size' => 5)
		);

		$this->form_list = array(
			'options_image_pref' => array(
				'images' => array(
					'title' =>	$this->l('Images'),
					'icon' =>	'tab-orders',
					'class' =>	'width4',
					'top' => '',
					'bottom' => '',
					'description' => $this->l('JPEG images have a small file size and standard quality. PNG images have a bigger file size, a higher quality and support transparency. Note that in all cases the image files will have the .jpg extension.').'
									  <br /><br />'.$this->l('WARNING: This feature may not be compatible with your theme or with some modules. In particular, PNG mode is not compatible with the Watermark module. If you encounter any issue, turn it off by selecting "Use JPEG".'),
					'fields' =>	array(
						'PS_IMAGE_QUALITY' => array('title' => $this->l('Image quality'), 'show' => true, 'required' => true, 'type' => 'radio', 'choices' => array('jpg' => $this->l('Use JPEG'), 'png' => $this->l('Use PNG only if the base image is in PNG format'), 'png_all' => $this->l('Use PNG for all images'))),
						'PS_JPEG_QUALITY' => array('title' => $this->l('JPEG quality'), 'desc' => $this->l('Ranges from 0 (worst quality, smallest file) to 100 (best quality, biggest file)'), 'validation' => 'isUnsignedId', 'required' => true, 'cast' => 'intval', 'type' => 'text'),
			 			'PS_PNG_QUALITY' => array('title' => $this->l('PNG quality'), 'desc' => $this->l('Ranges from 9 (worst quality, smallest file) to 0 (best quality, biggest file)'), 'validation' => 'isUnsignedId', 'required' => true, 'cast' => 'intval', 'type' => 'text'),
					),
				),
			)
		);

		$this->fields_form = array(
			'legend' => array(
				'title' => $this->l('Images'),
				'image' => '../img/admin/picture.gif'
			),
			'input' => array(
				array(
					'type' => 'text',
					'label' => $this->l('Type name:'),
					'name' => 'name',
					'required' => true,
					'p' => $this->l('Letters only (e.g., small, medium, large, extra-large)')
				),
				array(
					'type' => 'text',
					'label' => $this->l('Width:'),
					'name' => 'width',
					'required' => true,
					'size' => 4,
					'maxlength' => 5,
					'p' => $this->l('Maximum image width in pixels')
				),
				array(
					'type' => 'text',
					'label' => $this->l('Height:'),
					'name' => 'height',
					'required' => true,
					'size' => 4,
					'maxlength' => 5,
					'p' => $this->l('Maximum image height in pixels')
				),
				array(
					'type' => 'radio',
					'label' => $this->l('Products:'),
					'name' => 'products',
					'required' => false,
					'class' => 't',
					'is_bool' => true,
					'p' => $this->l('This type will be applied to product images'),
					'values' => array(
						array(
							'id' => 'products_on',
							'value' => 1,
							'label' => $this->l('Enabled')
						),
						array(
							'id' => 'products_off',
							'value' => 0,
							'label' => $this->l('Disabled')
						),
					)
				),
				array(
					'type' => 'radio',
					'label' => $this->l('Categories:'),
					'name' => 'categories',
					'required' => false,
					'class' => 't',
					'is_bool' => true,
					'p' => $this->l('This type will be applied to categories images'),
					'values' => array(
						array(
							'id' => 'categories_on',
							'value' => 1,
							'label' => $this->l('Enabled')
						),
						array(
							'id' => 'categories_off',
							'value' => 0,
							'label' => $this->l('Disabled')
						),
					)
				),
				array(
					'type' => 'radio',
					'label' => $this->l('Manufacturers:'),
					'name' => 'manufacturers',
					'required' => false,
					'class' => 't',
					'is_bool' => true,
					'p' => $this->l('This type will be applied to manufacturers images'),
					'values' => array(
						array(
							'id' => 'manufacturers_on',
							'value' => 1,
							'label' => $this->l('Enabled')
						),
						array(
							'id' => 'manufacturers_off',
							'value' => 0,
							'label' => $this->l('Disabled')
						),
					)
				),
				array(
					'type' => 'radio',
					'label' => $this->l('Suppliers:'),
					'name' => 'suppliers',
					'required' => false,
					'class' => 't',
					'is_bool' => true,
					'p' => $this->l('This type will be applied to suppliers images'),
					'values' => array(
						array(
							'id' => 'suppliers_on',
							'value' => 1,
							'label' => $this->l('Enabled')
						),
						array(
							'id' => 'suppliers_off',
							'value' => 0,
							'label' => $this->l('Disabled')
						),
					)
				),
				array(
					'type' => 'radio',
					'label' => $this->l('Scenes:'),
					'name' => 'scenes',
					'required' => false,
					'class' => 't',
					'is_bool' => true,
					'p' => $this->l('This type will be applied to scenes images'),
					'values' => array(
						array(
							'id' => 'scenes_on',
							'value' => 1,
							'label' => $this->l('Enabled')
						),
						array(
							'id' => 'scenes_off',
							'value' => 0,
							'label' => $this->l('Disabled')
						),
					)
				),
				array(
					'type' => 'radio',
					'label' => $this->l('stores:'),
					'name' => 'stores',
					'required' => false,
					'class' => 't',
					'is_bool' => true,
					'p' => $this->l('This type will be applied to stores images'),
					'values' => array(
						array(
							'id' => 'stores_on',
							'value' => 1,
							'label' => $this->l('Enabled')
						),
						array(
							'id' => 'stores_off',
							'value' => 0,
							'label' => $this->l('Disabled')
						),
					)
				),
			),
			'submit' => array(
				'title' => $this->l('   Save   '),
				'class' => 'button'
			)
		);


		parent::__construct();
	}

	public function postProcess()
	{
		if (Tools::getValue('submitRegenerate'.$this->table))
		{
		 	if ($this->tabAccess['edit'] === '1')
		 	{
				if ($this->_regenerateThumbnails(Tools::getValue('type'), Tools::getValue('erase')))
					Tools::redirectAdmin(self::$currentIndex.'&conf=9'.'&token='.$this->token);
			}
			else
				$this->_errors[] = Tools::displayError('You do not have permission to edit here.');
		}elseif (Tools::getValue('submitMoveImages'.$this->table))
		{
			if ($this->tabAccess['edit'] === '1')
		 	{
				if($this->_moveImagesToNewFileSystem())
					Tools::redirectAdmin(self::$currentIndex.'&conf=25'.'&token='.$this->token);
		 	}
		else
				$this->_errors[] = Tools::displayError('You do not have permission to edit here.');
		}elseif (Tools::getValue('submitImagePreferences'))
		{
			if ($this->tabAccess['edit'] === '1')
			{
				if ((int)Tools::getValue('PS_JPEG_QUALITY') < 0
					|| (int)Tools::getValue('PS_JPEG_QUALITY') > 100)
					$this->_errors[] = Tools::displayError('Incorrect value for JPEG image quality.');
				elseif ((int)Tools::getValue('PS_PNG_QUALITY') < 0
					|| (int)Tools::getValue('PS_PNG_QUALITY') > 9)
					$this->_errors[] = Tools::displayError('Incorrect value for PNG image quality.');
				elseif (!Configuration::updateValue('PS_IMAGE_QUALITY', Tools::getValue('PS_IMAGE_QUALITY'))
					|| !Configuration::updateValue('PS_JPEG_QUALITY', Tools::getValue('PS_JPEG_QUALITY'))
					|| !Configuration::updateValue('PS_PNG_QUALITY', Tools::getValue('PS_PNG_QUALITY')))
					$this->_errors[] = Tools::displayError('Unknown error.');
				else
					Tools::redirectAdmin(self::$currentIndex.'&token='.Tools::getValue('token').'&conf=4');
			}
			else
				$this->_errors[] = Tools::displayError('You do not have permission to edit here.');
		}
		else
			parent::postProcess();
	}

	protected function _childValidation()
	{
		if (!Tools::getValue('id_image_type') AND Validate::isImageTypeName($typeName = Tools::getValue('name')) AND ImageType::typeAlreadyExists($typeName))
			$this->_errors[] = Tools::displayError('This name already exists.');
	}

	/**
	  * Display form for thumbnails regeneration
	  */
	public function displayRegenerate()
	{
		$types = array(
			'categories' => $this->l('Categories'),
			'manufacturers' => $this->l('Manufacturers'),
			'suppliers' => $this->l('Suppliers'),
			'scenes' => $this->l('Scenes'),
			'products' => $this->l('Products'),
			'stores' => $this->l('Stores')
		);
		$this->content .= '
		<h2 class="space">'.$this->l('Regenerate thumbnails').'</h2>
		'.$this->l('Regenerates thumbnails for all existing product images').'.<br /><br /><div  class="width4">';
		$this->content .= '<div class="warn">'.$this->l('Please be patient, as this can take several minutes').'<br />'.$this->l('Be careful! Manually generated thumbnails will be erased by automatically generated thumbnails.').'</div>';
		$this->content .= '</div>

		<form action="'.self::$currentIndex.'&token='.$this->token.'" method="post">

			<fieldset class="width4">
				<legend><img src="../img/admin/picture.gif" /> '.$this->l('Regenerate thumbnails').'</legend><br />
				<label>'.$this->l('Select image').'</label>
				<div class="margin-form">
					<select name="type" onchange="changeFormat(this)">
						<option value="all">'.$this->l('All').'</option>';
				foreach ($types AS $k => $type)
					$this->content .= '<option value="'.$k.'">'.$type.'</option>';
				$this->content .= '
					</select>
				</div>';

			foreach ($types AS $k => $type)
			{
				$formats = ImageType::getImagesTypes($k);
				$this->content .= '
				<label class="second-select format_'.$k.'" style="display:none;">'.$this->l('Select format').'</label>
				<div class="second-select margin-form format_'.$k.'" style="display:none;">
				<select class="second-select format_'.$k.'" name="format_'.$k.'">
					<option value="all">'.$this->l('All').'</option>';
				foreach ($formats AS $format)
					$this->content .= '<option value="'.$format['id_image_type'].'">'.$format['name'].'</option>';
				$this->content .= '</select></div>';
			}
			$this->content .= '
				<script>
					function changeFormat(elt)
					{
						$(\'.second-select\').hide();
						$(\'.format_\' + $(elt).val()).show();
					}
				</script>
				<label>'.$this->l('Erase previous images').'</label>
				<div class="margin-form">
					<input name="erase" type="checkbox" value="1" checked="checked" />
					<p>'.$this->l('Uncheck this checkbox only if your server timed out and you need to resume the regeneration.').'</p>
				</div>
				<div class="clear"></div>
				<center><input type="Submit" name="submitRegenerate'.$this->table.'" value="'.$this->l('Regenerate thumbnails').'" class="button space" onclick="return confirm(\''.$this->l('Are you sure?', __CLASS__, true, false).'\');" /></center>
			</fieldset>
		</form>';
	}

	/**
	  * Delete resized image then regenerate new one with updated settings
	  */
	private function _deleteOldImages($dir, $type, $product = false)
	{
		if (!is_dir($dir))
			return false;
		$toDel = scandir($dir);
		foreach ($toDel AS $d)
			foreach ($type AS $imageType)
				if (preg_match('/^[0-9]+\-'.($product ? '[0-9]+\-' : '').$imageType['name'].'\.jpg$/', $d) OR preg_match('/^([[:lower:]]{2})\-default\-(.*)\.jpg$/', $d))
					if (file_exists($dir.$d))
						unlink($dir.$d);

		// delete product images using new filesystem.
		if ($product)
		{
			$productsImages = Image::getAllImages();
			foreach ($productsImages AS $k => $image)
			{
				$imageObj = new Image($image['id_image']);
				$imageObj->id_product = $image['id_product'];
				if (file_exists($dir.$imageObj->getImgFolder()))
				{
					$toDel = scandir($dir.$imageObj->getImgFolder());
					foreach ($toDel AS $d)
						foreach ($type AS $imageType)
							if (preg_match('/^[0-9]+\-'.$imageType['name'].'\.jpg$/', $d))
								if (file_exists($dir.$imageObj->getImgFolder().$d))
									unlink($dir.$imageObj->getImgFolder().$d);
	}
			}
		}
	}

	// Regenerate images
	private function _regenerateNewImages($dir, $type, $productsImages = false)
	{
		if (!is_dir($dir))
			return false;
		$errors = false;
		$toRegen = scandir($dir);
		if (!$productsImages)
		{
			foreach ($toRegen AS $image)
				if (preg_match('/^[0-9]*\.jpg$/', $image))
					foreach ($type AS $k => $imageType)
					{
						// Customizable writing dir
						$newDir = $dir;
						if ($imageType['name'] == 'thumb_scene')
							$newDir .= 'thumbs/';
						if (!file_exists($newDir))
							continue;
						if (!file_exists($newDir.substr($image, 0, -4).'-'.stripslashes($imageType['name']).'.jpg'))
							if (!imageResize($dir.$image, $newDir.substr($image, 0, -4).'-'.stripslashes($imageType['name']).'.jpg', (int)($imageType['width']), (int)($imageType['height'])))
								$errors = true;
						if (time() - $this->start_time > $this->max_execution_time - 4) // stop 4 seconds before the tiemout, just enough time to process the end of the page on a slow server
							return 'timeout';
					}
		}
		else
		{
			$productsImages = Image::getAllImages();
			foreach ($productsImages AS $k => $image)
			{
				$imageObj = new Image($image['id_image']);
				if (file_exists($dir.$imageObj->getExistingImgPath().'.jpg'))
					foreach ($type AS $k => $imageType)
					{
						if (!file_exists($dir.$imageObj->getExistingImgPath().'-'.stripslashes($imageType['name']).'.jpg'))
							if (!imageResize($dir.$imageObj->getExistingImgPath().'.jpg', $dir.$imageObj->getExistingImgPath().'-'.stripslashes($imageType['name']).'.jpg', (int)($imageType['width']), (int)($imageType['height'])))
								$errors = true;
						if (time() - $this->start_time > $this->max_execution_time - 4) // stop 4 seconds before the tiemout, just enough time to process the end of the page on a slow server
							return 'timeout';
					}
		}
		}
		return $errors;
	}

	// Regenerate no-pictures images
	private function _regenerateNoPictureImages($dir, $type, $languages)
	{
		$errors = false;
		foreach ($type AS $k => $imageType)
			foreach ($languages AS $language)
			{
				$file = $dir.$language['iso_code'].'.jpg';
				if (!file_exists($file))
					$file = _PS_PROD_IMG_DIR_.Language::getIsoById((int)(Configuration::get('PS_LANG_DEFAULT'))).'.jpg';
				if (!file_exists($dir.$language['iso_code'].'-default-'.stripslashes($imageType['name']).'.jpg'))
					if (!imageResize($file, $dir.$language['iso_code'].'-default-'.stripslashes($imageType['name']).'.jpg', (int)($imageType['width']), (int)($imageType['height'])))
						$errors = true;
			}
		return $errors;
	}

	// Hook watermark optimization
	private function _regenerateWatermark($dir)
	{
		$result = Db::getInstance()->executeS('
		SELECT m.`name` FROM `'._DB_PREFIX_.'module` m
		LEFT JOIN `'._DB_PREFIX_.'hook_module` hm ON hm.`id_module` = m.`id_module`
		LEFT JOIN `'._DB_PREFIX_.'hook` h ON hm.`id_hook` = h.`id_hook`
		WHERE h.`name` = \'watermark\' AND m.`active` = 1');

		if ($result AND sizeof($result))
		{
			$productsImages = Image::getAllImages();
			foreach ($productsImages AS $k => $image)
			{
				$imageObj = new Image($image['id_image']);
				if (file_exists($dir.$imageObj->getExistingImgPath().'.jpg'))
					foreach ($result AS $k => $module)
					{
						if ($moduleInstance = Module::getInstanceByName($module['name']) AND is_callable(array($moduleInstance, 'hookwatermark')))
							call_user_func(array($moduleInstance, 'hookwatermark'), array('id_image' => $imageObj->id, 'id_product' => $imageObj->id_product));
						if (time() - $this->start_time > $this->max_execution_time - 4) // stop 4 seconds before the tiemout, just enough time to process the end of the page on a slow server
							return 'timeout';
					}
		}
	}
	}

	private function _regenerateThumbnails($type = 'all', $deleteOldImages = false)
	{
		$this->start_time = time();
		ini_set('max_execution_time', $this->max_execution_time); // ini_set may be disabled, we need the real value
		$this->max_execution_time = (int)ini_get('max_execution_time');
		$languages = Language::getLanguages(false);

		$process =
			array(
				array('type' => 'categories', 'dir' => _PS_CAT_IMG_DIR_),
				array('type' => 'manufacturers', 'dir' => _PS_MANU_IMG_DIR_),
				array('type' => 'suppliers', 'dir' => _PS_SUPP_IMG_DIR_),
				array('type' => 'scenes', 'dir' => _PS_SCENE_IMG_DIR_),
				array('type' => 'products', 'dir' => _PS_PROD_IMG_DIR_),
				array('type' => 'stores', 'dir' => _PS_STORE_IMG_DIR_)
			);

		// Launching generation process
		foreach ($process AS $k => $proc)
		{
			if ($type != 'all' && $type != $proc['type'])
				continue ;

			// Getting format generation
			$formats = ImageType::getImagesTypes($proc['type']);
			if ($type != 'all')
			{
				$format = strval(Tools::getValue('format_'.$type));
				if ($format != 'all')
					foreach ($formats AS $k => $form)
						if ($form['id_image_type'] != $format)
							unset($formats[$k]);
			}

			if ($deleteOldImages)
				$this->_deleteOldImages($proc['dir'], $formats, ($proc['type'] == 'products' ? true : false));
			if (($return = $this->_regenerateNewImages($proc['dir'], $formats, ($proc['type'] == 'products' ? true : false))) === true)
				$this->_errors[] = Tools::displayError('Cannot write ').$proc['type'].Tools::displayError(' images. Please check the folder\'s writing permissions.');
			elseif ($return == 'timeout')
				$this->_errors[] = Tools::displayError('Only part of the images have been regenerated, server timed out before finishing.');
			else
			{
				if ($proc['type'] == 'products')
					if ($this->_regenerateWatermark($proc['dir']) == 'timeout')
						$this->_errors[] = Tools::displayError('Server timed out, the watermark may not have been applied on all your images.');
				if (!count($this->_errors))
					if ($this->_regenerateNoPictureImages($proc['dir'], $formats, $languages))
						$this->_errors[] = Tools::displayError('Cannot write no-picture image to').' ('.$proc['type'].') '.Tools::displayError('images folder. Please check the folder\'s writing permissions.');
			}
		}
		return (sizeof($this->_errors) > 0 ? false : true);
	}

	/**
	 * Display the block for moving images
	 */
	public function displayMoveImages()
	{
		$safe_mode = ini_get('safe_mode');

		$this->content .= '
		<br /><h2 class="space">'.$this->l('Move images').'</h2>'.
		$this->l('A new storage system for product images is now used by PrestaShop. It offers better performance if your shop has a very large number of products.').'<br />'.
		'<br />';
		if (file_exists(_PS_PROD_IMG_DIR_.'duplicates/'))
		{
			$this->content .= '<div class="width4">';
			$this->displayWarning($this->l('Duplicate images were found when moving the product images. It is probably caused by unused demonstration images. Please make sure that the folder ').
				_PS_PROD_IMG_DIR_.'duplicates/'.
				$this->l(' only contains demonstration images then delete this folder.'));
			$this->content .= '</div>';
		}
		if($safe_mode)
			$this->displayWarning($this->l('PrestaShop has detected that your server configuration is not compatible with the new storage system (directive "safe_mode" is activated). You should continue to use the actual system.'));
		else
			$this->content .= '
		<form action="'.self::$currentIndex.'&token='.$this->token.'" method="post">
				<fieldset class="width4">
				<legend><img src="../img/admin/picture.gif" /> '.$this->l('Move images').'</legend><br />'.
				$this->l('You can choose to keep your images stored in the previous system - nothing wrong with that.').'<br />'.
				$this->l('You can also decide to move your images to the new storage system: in this case, click on the "Move images" button below.	Please be patient, as this can take several minutes.').
				'<br /><br /><div class="hint clear" style="display: block;">&nbsp;'.
				$this->l('After moving all of your product images, for best performance go to the ').
				'<a style="text-decoration:underline" href="index.php?tab=AdminPPreferences&token='.Tools::getAdminTokenLite('AdminPPreferences').'#PS_LEGACY_IMAGES_on">'.$this->l('product preferences tab').'</a>'.
					$this->l(' and set "Use the legacy image filesystem" to NO.').'
				</div>
				<center><input type="Submit" name="submitMoveImages'.$this->table.'" value="'.$this->l('Move images').'" class="button space" onclick="return confirm(\''.$this->l('Are you sure?', __CLASS__, true, false).'\');" /></center>
			</fieldset>
		</form>';
}

	/**
	 * Move product images to the new filesystem
	 */
	private function _moveImagesToNewFileSystem()
	{
		if (!Image::testFileSystem())
			$this->_errors[] =  Tools::displayError('Error: your server configuration is not compatible with the new image system. No images were moved');
		else
		{
			ini_set('max_execution_time', $this->max_execution_time); // ini_set may be disabled, we need the real value
			$this->max_execution_time = (int)ini_get('max_execution_time');
			$result = Image::moveToNewFileSystem($this->max_execution_time);
			if ($result === 'timeout')
				$this->_errors[] =  Tools::displayError('Not all images have been moved, server timed out before finishing. Click on \"Move images\" again to resume moving images');
			else if ($result === false)
				$this->_errors[] =  Tools::displayError('Error: some or all images could not be moved.');
		}
		return (sizeof($this->_errors) > 0 ? false : true);
	}

	public function initContent()
	{
		if ($this->display != 'edit' && $this->display != 'add')
			$this->display = 'list';

		parent::initContent();

		if ($this->display == 'list')
		{
			$this->context->smarty->assign('list', true);

			$helper = new HelperOptions();
			$helper->id = $this->id;
			$helper->currentIndex = self::$currentIndex;
			$this->content .= $helper->generateOptions($this->form_list['options_image_pref']);

			$this->displayRegenerate();
			$this->displayMoveImages();
		}

		if ($this->display == 'edit')
			$this->warnings[] = $this->l('After modification, do not forget to regenerate thumbnails');
	}
}