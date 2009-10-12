package jflowmap;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

/**
 * @author Ilya Boyandin
 */
public class FlowMapMain extends JFrame {

    private static Logger logger = Logger.getLogger(FlowMapMain.class);
    
    private static final long serialVersionUID = 1L;
    public static final String OS_NAME = System.getProperty("os.name");
    public static boolean IS_OS_MAC = getOSMatches("Mac");

    private static boolean getOSMatches(String osNamePrefix) {
        if (OS_NAME == null) {
            return false;
        }
        return OS_NAME.startsWith(osNamePrefix);
    }

    private JFlowMap flowMap;

    public FlowMapMain() {
        setTitle("JFlowMap");
        flowMap = new JFlowMap(this);
        add(flowMap);

        setExtendedState(MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(800, 600));
//        setPreferredSize(new Dimension(800, 600));
//        pack();

        final Dimension size = getSize();
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final int locX = (screen.width - size.width) / 2;
        final int locY = (screen.height - size.height) / 2;
        setLocation(locX, locY);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public static void main(String[] args) throws IOException {
        logger.info(">>> Starting JFlowMap");
        initLookAndFeel();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new FlowMapMain().setVisible(true);
            }
        });
    }

    private static void initLookAndFeel() {
//        try {
//            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
