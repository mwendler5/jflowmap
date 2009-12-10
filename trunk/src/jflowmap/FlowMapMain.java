package jflowmap;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalTabbedPaneUI;

import org.apache.log4j.Logger;

import at.fhj.utils.swing.JMemoryIndicator;

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

    private final JFlowMap flowMap;

    public FlowMapMain() {
        setTitle("JFlowMap");
        flowMap = new JFlowMap(this);
        add(flowMap);

        JPanel statusPanel = new JPanel(new BorderLayout());
        add(statusPanel, BorderLayout.SOUTH);

        JMemoryIndicator mi = new JMemoryIndicator(3000);
        statusPanel.add(mi, BorderLayout.EAST);
        mi.startUpdater();

        setExtendedState(MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(800, 600));
//        setPreferredSize(new Dimension(800, 600));
//        pack();

        final Dimension size = getSize();
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final int locX = (screen.width - size.width) / 2;
        final int locY = (screen.height - size.height) / 2;
        setLocation(locX, locY);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                shutdown();
            }
        });
    }

    public void shutdown() {
        logger.info("Exiting application");
        System.exit(0);
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
////            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
