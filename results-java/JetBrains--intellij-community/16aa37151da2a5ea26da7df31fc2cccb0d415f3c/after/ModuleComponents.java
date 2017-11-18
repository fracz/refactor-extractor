// Generated on Wed Nov 07 17:26:02 MSK 2007
// DTD/Schema  :    plugin.dtd

package org.jetbrains.idea.devkit.dom;

import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * plugin.dtd:module-components interface.
 */
public interface ModuleComponents extends DomElement {

	/**
	 * Returns the list of component children.
	 * @return the list of component children.
	 */
	@NotNull
	List<Component.Module> getComponents();
	/**
	 * Adds new child to the list of component children.
	 * @return created child
	 */
	Component.Module addComponent();


}