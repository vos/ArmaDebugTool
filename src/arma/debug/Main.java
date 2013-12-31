package arma.debug;

import java.awt.*;

public class Main {
    private static DebugWindow debugWindow;

    public static String run(final String arg) {
        if (!arg.startsWith("LOG:")) {
            return "ERROR";
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (debugWindow == null) {
                    debugWindow = DebugWindow.createAndShow();
                } else if (!debugWindow.isShowing()) {
                    debugWindow.setVisible(true);
                }
                String logMessage = arg.substring(4);
                debugWindow.log(logMessage);
            }
        });
        return "PASS";
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            String cmd = "LOG:Lorem ipsum dolor sit amet consectetuer adipiscing elit " + i;
            String ret = run(cmd);
            System.out.println(cmd + " -> " + ret);
            Thread.sleep(1000);
        }
    }
}
