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

package jflowmap.visuals;

import jflowmap.models.map.Area;
import jflowmap.models.map.AreaMap;
import edu.umd.cs.piccolo.PNode;

/**
 * @author Ilya Boyandin
 */
public class VisualAreaMap extends PNode {

    private static final long serialVersionUID = -6938288211394773330L;

    public VisualAreaMap(VisualFlowMap visualFlowMap, AreaMap mapModel) {
        for (Area area : mapModel.getAreas()) {
            addChild(new VisualArea(visualFlowMap, area));
        }
    }

}
