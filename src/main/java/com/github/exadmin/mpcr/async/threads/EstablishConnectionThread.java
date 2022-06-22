package com.github.exadmin.mpcr.async.threads;

import com.github.exadmin.mpcr.async.MyRunnable;
import com.github.exadmin.mpcr.fxui.FxSceneModel;
import com.github.exadmin.mpcr.misc.FileUtils;
import com.github.exadmin.mpcr.misc.Settings;
import com.github.exadmin.mpcr.misc.StrUtils;
import javafx.application.Platform;

import java.io.*;

public class EstablishConnectionThread extends MyRunnable {
    private static final String CMD_SHORT_FILENAME = ".\\vpncli.cmd";

    private boolean printToConsole = false;

    public EstablishConnectionThread(FxSceneModel fxSceneModel, MyRunnable nextThreadToStart) {
        super(fxSceneModel, nextThreadToStart);
    }

    public EstablishConnectionThread(FxSceneModel fxSceneModel, MyRunnable nextThreadToStart, boolean printToConsole) {
        super(fxSceneModel, nextThreadToStart);
        this.printToConsole = printToConsole;
    }

    @Override
    protected void runSafe() throws Exception {
        // create cmd-file to be executed
        File cmdFile = new File(CMD_SHORT_FILENAME);
        try (PrintWriter pw = new PrintWriter(cmdFile)) {
            if (Settings.isAutoStopEnabled()) {
                pw.println("taskkill /F /IM vpnagent.exe /IM vpnui.exe");
            }

            pw.println("\"" + Settings.getVpncliPath() + "\" -s < %1");
            pw.println("\"" + FileUtils.getFolderOnly(Settings.getVpncliPath()) + "\\vpnui.exe" + "\"");
        }

        // create parameters file to be passed as argument into executable file
        File tmpCfgFile = File.createTempFile("mpass", "");
        tmpCfgFile.deleteOnExit();

        try (PrintWriter pw = new PrintWriter(tmpCfgFile)) {
            pw.println("connect " + Settings.getVpnHost());
            pw.println(Settings.getNtLogin());
            char[] chars = Settings.getNtPassword().toCharArray();
            for (char ch : chars) {
                pw.print(ch);
            }
            pw.println();
            pw.println(fxSceneModel.pinCode.getValue());
            pw.println("y");
            pw.println("exit");
        }

        Runtime rt = Runtime.getRuntime();
        String[] commands = {CMD_SHORT_FILENAME, tmpCfgFile.getAbsolutePath()};
        Process process = rt.exec(commands);

        // OutputStream stdin = process.getOutputStream ();
        // InputStream stderr = process.getErrorStream ();
        InputStream stdout = process.getInputStream ();

        BufferedReader reader = new BufferedReader (new InputStreamReader(stdout));

        String line;
        while ((line = reader.readLine ()) != null) {
            line = line.trim();
            if (StrUtils.isStringEmpty(line, false)) continue;

            printlnToFxConsole(line);

            if (line.equals("goodbye...")) break;
        }
    }

    private void printlnToFxConsole(String message) {
        if (printToConsole) {
            System.out.println(message);
        } else {
            Platform.runLater(() -> {
                fxSceneModel.getTextArea().appendText(message + "\n");

                // attempt to scroll down - but currently does not work, todo: fix
                fxSceneModel.getTextArea().setScrollTop(Double.MAX_VALUE);
            });
        }
    }
}
