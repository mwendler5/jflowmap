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
import javax.swing.JSplitPane;
import javax.swing.UIManager;

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
        FlowMapCanvas canvas = new FlowMapCanvas(graph, valueAttrName, labelAttrName);
        add(canvas);
        add(new ControlPanel(canvas), BorderLayout.SOUTH);
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
        initLookAndFeel();
        final GraphMLReader reader = new GraphMLReader();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
//                    new FlowMapMain(reader.readGraph("data/migrations.xml"), "value", "tooltip").setVisible(true);
                  new FlowMapMain(reader.readGraph("data/refugee-flows-2008.xml"), "refugees", "name").setVisible(true);
                } catch (DataIOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        });

    }

    static class DatasetSpec {
        private String filename;
        private String valueAttrName;
        private String labelAttrName;
    }

    private static void initLookAndFeel() {
        try {
//            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
