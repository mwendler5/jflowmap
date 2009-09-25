package ch.unifr.flowmap.visuals;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PPanEventHandler;

public class PanHandler extends PPanEventHandler {
    protected void drag(PInputEvent e) {
        if (!e.isControlDown()) {
            super.drag(e);
        }
    }

    protected void dragActivityFirstStep(PInputEvent aEvent) {
        if (!aEvent.isControlDown()) {
            super.dragActivityFirstStep(aEvent);
        }
    }

    protected void dragActivityStep(PInputEvent aEvent) {
        if (!aEvent.isControlDown()) {
            super.dragActivityStep(aEvent);
        }
    }
}
