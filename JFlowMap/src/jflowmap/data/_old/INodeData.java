/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jflowmap.data._old;

import java.awt.geom.Point2D;

/**
 * @author Ilya Boyandin
 */
public interface INodeData {

	int numNodes();

	String nodeId(int nodeIdx);
	
	double nodeX(int nodeIdx);

	double nodeY(int nodeIdx);

	Point2D.Double nodePosition(int nodeIdx);

	String nodeLabel(int nodeIdx);

//	int numAttrs();
//
//	String getAttrLabel(int attrIdx);
//	
//	INodeAttrValue<?> getAttrValue(int nodeIdx, int attrIdx);

}
