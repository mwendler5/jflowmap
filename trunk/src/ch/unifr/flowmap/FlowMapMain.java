package ch.unifr.flowmap;

import ch.unifr.flowmap.ui.ControlPanel;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import prefuse.data.Graph;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import ch.unifr.flowmap.ui.FlowMapCanvas;
import java.awt.BorderLayout;

/**
 * @author Ilya Boyandin
 */
public class FlowMapMain extends JFrame {

    private static final long serialVersionUID = 1L;
    public static final String OS_NAME = System.getProperty("os.name");
    public static boolean IS_OS_MAC = getOSMatches("Mac");

    private static boolean getOSMatches(String osNamePrefix) {
        if (OS_NAME == null) {
            return false;
        }
        return OS_NAME.startsWith(osNamePrefix);
    }

    public FlowMapMain(Graph graph, String valueAttrName, String labelAttrName) {
        setTitle("FlowMap");
        setLayout(new BorderLayout());
        add(new ControlPanel(), BorderLayout.NORTH);
        add(new FlowMapCanvas(graph, valueAttrName, labelAttrName));
//		add(new JPanel());
        setPreferredSize(new Dimension(800, 600));
        pack();

        final Dimension size = getSize();
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final int locX = (screen.width - size.width) / 2;
        final int locY = (screen.height - size.height) / 2;
        setLocation(locX, locY);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public static void main(String[] args) throws DataIOException {
        final GraphMLReader reader = new GraphMLReader();

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
//            		new FlowMapMain(reader.readGraph("data/migrations.xml"), "value", "tooltip").setVisible(true);
                    new FlowMapMain(reader.readGraph("data/refugee-flows-2008.xml"), "refugees", "name").setVisible(true);
                } catch (DataIOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        });

    }
}
