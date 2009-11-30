package jflowmap;

import javax.swing.JApplet;

/**
 * @author Ilya Boyandin
 */
public class FlowMapApplet extends JApplet {

    private static final long serialVersionUID = 1778664403741899654L;

    public FlowMapApplet() {
        add(new JFlowMap(null));
    }
}
