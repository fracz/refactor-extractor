package com.thinkaurelius.titan.graphdb.edgetypes;

import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.graphdb.edgetypes.manager.EdgeTypeManager;
import com.thinkaurelius.titan.graphdb.edgetypes.system.SystemEdgeTypeManager;
import com.thinkaurelius.titan.graphdb.transaction.GraphTx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StandardEdgeTypeMaker implements EdgeTypeMaker {

	private final GraphTx tx;
	private final EdgeTypeManager etManager;

	private EdgeTypeVisibility visibility;
	private String name;
	private EdgeTypeGroup group;
	private boolean isFunctional;
	private Directionality directionality;
	private EdgeCategory category;
	private List<EdgeType> keysig;
	private List<EdgeType> compactsig;

	private boolean hasIndex;
	private boolean isKey;
	private Class<?> objectType;

	public StandardEdgeTypeMaker(GraphTx tx, EdgeTypeManager etManager) {
		this.tx=tx;
		this.etManager = etManager;

		//Default assignments
		objectType = null;
		name = null;
		hasIndex = false;
		isKey = false;
		keysig = new ArrayList<EdgeType>();
		compactsig = new ArrayList<EdgeType>();
		category = EdgeCategory.Labeled;
		directionality = Directionality.Directed;
		isFunctional = false;
		group = EdgeTypeGroup.DefaultGroup;
		visibility = EdgeTypeVisibility.Modifiable;
	}

	private void checkGeneralArguments() {
		if (name==null || name.length()==0)
			throw new IllegalArgumentException("Need to specify name.");
		if (name.startsWith(SystemEdgeTypeManager.systemETprefix))
			throw new IllegalArgumentException("Name starts with a reserved keyword!");
		if ((!keysig.isEmpty() || !compactsig.isEmpty()) && category!=EdgeCategory.Labeled)
			throw new IllegalArgumentException("Can only specify signatures for labeled edge types.");
	}

	private EdgeType[] checkSignature(List<EdgeType> sig) {
		EdgeType[] signature = new EdgeType[sig.size()];
		for (int i=0;i<sig.size();i++) {
			EdgeType et = sig.get(i);
			if (!et.isFunctional())
				throw new IllegalArgumentException("Signature edge types must be functional :" + et);
			if (et.getCategory()!=EdgeCategory.Simple)
				throw new IllegalArgumentException("Signature edge types must be simple: " + et);
			if (et.isRelationshipType() && et.getDirectionality()!=Directionality.Unidirected)
				throw new IllegalArgumentException("Signature relationship types must be unidirected: " + et);
			signature[i]=et;
		}
		return signature;
	}

	@Override
	public PropertyType makePropertyType() {
		checkGeneralArguments();
		if (directionality!=Directionality.Directed)
			throw new IllegalArgumentException("Property types must be directed!");
		if (category!=EdgeCategory.Simple)
			throw new IllegalArgumentException("Only simple properties are supported!");
		if (objectType==null)
			throw new IllegalArgumentException("Need to specify data type.");
		if (isKey && !hasIndex)
			throw new IllegalArgumentException("Need to define an hasIndex for keyed PropertyType");
		return etManager.createPropertyType(tx, name, category, directionality,
				visibility, isFunctional, checkSignature(keysig), checkSignature(compactsig),
				group, isKey, hasIndex, objectType);
	}

	@Override
	public RelationshipType makeRelationshipType() {
		checkGeneralArguments();
		if (hasIndex)
			throw new IllegalArgumentException("Cannot specify hasIndex for relationship type.");
		return etManager.createRelationshipType(tx, name, category, directionality,
				visibility, isFunctional, checkSignature(keysig), checkSignature(compactsig), group);

	}

	@Override
	public StandardEdgeTypeMaker compactSignature(EdgeType... et) {
		compactsig = Arrays.asList(et);
		return this;
	}

	@Override
	public StandardEdgeTypeMaker keySignature(EdgeType... et) {
		keysig = Arrays.asList(et);
		return this;
	}

	@Override
	public StandardEdgeTypeMaker category(EdgeCategory cat) {
		category = cat;
		return this;
	}

	@Override
	public StandardEdgeTypeMaker dataType(Class<?> clazz) {
		objectType = clazz;
		return this;
	}

	@Override
	public StandardEdgeTypeMaker functional(boolean isfunctional) {
		isFunctional = isfunctional;
		return this;
	}

	@Override
	public StandardEdgeTypeMaker group(EdgeTypeGroup group) {
		this.group = group;
		return this;
	}

	@Override
	public StandardEdgeTypeMaker withDirectionality(Directionality dir) {
		directionality = dir;
		return this;
	}

	@Override
	public StandardEdgeTypeMaker withName(String name) {
		this.name = name;
		return this;
	}

	@Override
	public EdgeTypeMaker makeKeyed() {
		isKey = true;
		return this;
	}

	@Override
	public EdgeTypeMaker withIndex(boolean hasIndex) {
		this.hasIndex = hasIndex;
		return this;
	}

}