package jadx.gui.treemodel;

import jadx.api.JavaClass;
import jadx.api.JavaPackage;
import jadx.gui.utils.Utils;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;

public class JPackage extends JNode implements Comparable<JPackage> {
	private static final long serialVersionUID = -4120718634156839804L;

	private static final ImageIcon PACKAGE_ICON = Utils.openIcon("package_obj");

	private String name;
	private List<JClass> classes;
	private List<JPackage> innerPackages = new ArrayList<JPackage>(1);

	public JPackage(JavaPackage pkg) {
		this.name = pkg.getName();
		List<JavaClass> javaClasses = pkg.getClasses();
		this.classes = new ArrayList<JClass>(javaClasses.size());
		for (JavaClass javaClass : javaClasses) {
			classes.add(new JClass(javaClass));
		}
		updateChilds();
	}

	public JPackage(String name) {
		this.name = name;
		this.classes = new ArrayList<JClass>(1);
	}

	@Override
	public void updateChilds() {
		removeAllChildren();
		for (JPackage pkg : innerPackages) {
			pkg.updateChilds();
			add(pkg);
		}
		for (JClass cls : classes) {
			cls.updateChilds();
			add(cls);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<JPackage> getInnerPackages() {
		return innerPackages;
	}

	public List<JClass> getClasses() {
		return classes;
	}

	@Override
	public Icon getIcon() {
		return PACKAGE_ICON;
	}

	@Override
	public JClass getJParent() {
		return null;
	}

	@Override
	public int getLine() {
		return 0;
	}

	@Override
	public int compareTo(JPackage o) {
		return name.compareTo(o.name);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		return name.equals(((JPackage) o).name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}
}