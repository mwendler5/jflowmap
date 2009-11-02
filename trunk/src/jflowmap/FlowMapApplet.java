package jflowmap;

import javax.swing.JApplet;

/**
 * @author Ilya Boyandin
 */
public class FlowMapApplet extends JApplet {

    public FlowMapApplet() {
        add(new JFlowMap(null));
    }
}
