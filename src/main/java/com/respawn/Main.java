package com.respawn;

import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.net.http.HttpClient;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    private static final AtomicBoolean dialogOpen = new AtomicBoolean(false);
    private static final HttpClient client = HttpClient.newHttpClient();

    private static int apiFails = 0;
    private static HashSet<String> prevIDs = new HashSet<>();

    //private static boolean errorWindowOpened = false;

    /* Uses scheduler to run every 60 seconds 
     * Creates a set of the 20 most recent transaction IDs and compares with previous
     * The new IDs are then checked for printable status 
     * The returned offers are then printed 
     */
    public static void main(String[] args){
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        try {
            if (SystemTray.isSupported()) {
                SystemTray tray = SystemTray.getSystemTray();

                Image image = ImageIO.read(Main.class.getResource("/logo.png"));

                TrayIcon trayIcon = new TrayIcon(image, "Receipt Printer");
                trayIcon.setImageAutoSize(true);

                tray.add(trayIcon);
            }

        } catch (Throwable e) {
            handleError("Failed to generate tray image", e);
        }
        
        //try {ReceiptHandler.printReceipt(ReceiptHandler.makeTest());}catch(Exception e){}
        scheduler.scheduleAtFixedRate(() -> {
            try {
                String transactions = TransactionPoller.pollApi(client);
                apiFails = 0; // reset after a success
                HashSet<String> ids = TransactionInterpreter.getIDs(transactions);

                if (!prevIDs.isEmpty()) {
                    HashSet<String> newIds = new HashSet<>(ids);
                    newIds.removeAll(prevIDs);
                    for (Order o : TransactionInterpreter.removeOffers(transactions, newIds)) {
                        try {
                            System.out.println(o);
                            ReceiptHandler.printReceipt(o);
                        } catch (Exception e) {
                            handleError("Printing Receipt Failed", e);
                        }
                    }
                }
                prevIDs = ids;
            } catch (Throwable t) {
                String msg = String.valueOf(t.getMessage()); 
                System.out.println("Poll failed: " + t);
                if (msg.startsWith("Transactions GET fail") || msg.startsWith("JWT POST fail")) {
                    apiFails++;
                    if (apiFails > 5) handleError("API failing repeatedly", t);
                } else {
                    handleError("Main scheduler loop failed", t);
                }
            }
        }, 0, 60, TimeUnit.SECONDS);
    }

    private static void handleError(String context, Throwable t) {
        t.printStackTrace();
        if (!dialogOpen.compareAndSet(false, true)) return; 
        SwingUtilities.invokeLater(() -> {
            try {
                JOptionPane.showMessageDialog(null, context + "\n\n" + t,
                    "Respawn Error ( SCREENSHOT PLEASE, DON'T PANIC. IT WILL ALL BE OKAY :) )",
                    JOptionPane.ERROR_MESSAGE);
            } finally {
                dialogOpen.set(false);
            }
        });
    }

}

