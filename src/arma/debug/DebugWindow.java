package arma.debug;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class DebugWindow extends JFrame {
    private static final Font FONT = new Font("Courier New", Font.PLAIN, 12);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final int MAX_LOG_ENTRIES = 1000;

    private final JTextArea logTextArea;
    private final JTextField filterTextField;
    private final JLabel logLabel;

    private boolean autoScroll = true;
    private final LinkedList<LogEntry> logList = new LinkedList<>();
    private String filter = "";
    private int filterCount = 0;

    public DebugWindow() {
        super("ArmA Debug Tool v1.0 by Axe Cop");
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        setLayout(new BorderLayout(0, 0));

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(FONT);
        logTextArea.setBorder(new EmptyBorder(2, 2, 2, 2));
        logTextArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    JPopupMenu menu = new JPopupMenu("Log Menu");
                    final JCheckBoxMenuItem autoScrollMenuItem = new JCheckBoxMenuItem("Auto Scroll", autoScroll);
                    autoScrollMenuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            autoScroll = !autoScroll;
                            autoScrollMenuItem.setSelected(autoScroll);
                        }
                    });
                    menu.add(autoScrollMenuItem);
                    menu.addSeparator();
                    menu.add("Clear").addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            DebugWindow.this.clear();
                        }
                    });
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        add(new JScrollPane(logTextArea), BorderLayout.CENTER);

        filterTextField = new JTextField();
        filterTextField.setFont(FONT);
        filterTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                DebugWindow.this.updateFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                DebugWindow.this.updateFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                DebugWindow.this.updateFilter();
            }
        });

        Box box = Box.createHorizontalBox();
        box.add(filterTextField);
        logLabel = new JLabel();
        logLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
        updateLogLabel();
        box.add(logLabel);
        add(box, BorderLayout.SOUTH);
    }

    public void log(String message) {
        if (logList.size() == MAX_LOG_ENTRIES) {
            LogEntry logEntry = logList.removeFirst();
            if (matchesFilter(logEntry.message)) {
                try {
                    logTextArea.getDocument().remove(0, logEntry.toString().length());
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
                filterCount--;
            }
        }
        LogEntry logEntry = new LogEntry(message);
        logList.addLast(logEntry);
        if (matchesFilter(message)) {
            logTextArea.append(logEntry.toString());
            filterCount++;
        }
        if (autoScroll) {
            scrollToEnd();
        }
        updateLogLabel();
    }

    public void clear() {
        logList.clear();
        logTextArea.setText("");
        filterCount = 0;
        updateLogLabel();
    }

    private void scrollToEnd() {
        logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
    }

    private boolean matchesFilter(String s) {
        return filter.isEmpty() || s.toLowerCase().contains(filter);
    }

    private void updateFilter() {
        String filterText = filterTextField.getText().toLowerCase();
        if (!filterText.equals(filter)) {
            filter = filterText;
            logTextArea.setText("");
            filterCount = 0;
            for (LogEntry logEntry : logList) {
                if (matchesFilter(logEntry.message)) {
                    logTextArea.append(logEntry.toString());
                    filterCount++;
                }
            }
            if (autoScroll) {
                scrollToEnd();
            }
            updateLogLabel();
        }
    }

    private void updateLogLabel() {
        logLabel.setText(filterCount + " of " + logList.size());
    }

    public static DebugWindow createAndShow() {
        DebugWindow win = new DebugWindow();
        win.setSize(640, 480);
        win.setVisible(true);
        return win;
    }

    private static class LogEntry {
        final Date time;
        final String message;

        LogEntry(String message) {
            this.time = new Date();
            this.message = message;
        }

        @Override
        public String toString() {
            return DATE_FORMAT.format(time) + " - " + message + '\n';
        }
    }
}
