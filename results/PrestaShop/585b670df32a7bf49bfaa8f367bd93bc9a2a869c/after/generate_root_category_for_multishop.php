<?php

function generate_root_category_for_multishop()
{
	Db::getInstance()->execute('
		UPDATE `'._DB_PREFIX_.'category` SET `level_depth`=`level_depth`+1
	');

	Db::getInstance()->execute('
		INSERT INTO `'._DB_PREFIX_.'category` (`id_parent`, `level_depth`, `active`, `date_add`, `date_upd`, `is_root_category`) VALUES
		(0, 0, 1, NOW(), NOW(), 0)
	');
	$id = Db::getInstance()->Insert_ID();

	$langs = Db::getInstance()->executeS('
		SELECT `id_lang`
		FROM `'._DB_PREFIX_.'lang`
	');

	$shops = Db::getInstance()->executeS('
		SELECT `id_shop`
		FROM `'._DB_PREFIX_.'shop`
	');

	$data = array();
	foreach ($langs as $lang)
		foreach ($shops as $shop)
			$data[] = array(
				'id_lang' => $lang['id_lang'],
				'id_shop' => $shop['id_shop'],
				'id_category' => $id,
				'name' => 'Root',
				'link_rewrite' => '',
			);
	Db::getInstance()->autoExecute(_DB_PREFIX_.'category_lang', $data, 'INSERT');

	Db::getInstance()->execute('
		UPDATE `'._DB_PREFIX_.'category`
		SET `id_parent` = '.(int)$id.'
		WHERE `id_parent` = 0 AND `id_category` <> '.(int)$id.'
	');

	Category::regenerateEntireNtree();
}