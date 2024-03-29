package com.github.exadmin.mpcr.async.threads;

import com.github.exadmin.mpcr.async.MyRunnable;
import com.github.exadmin.mpcr.fxui.FxSceneModel;
import com.github.exadmin.mpcr.misc.FileUtils;
import com.github.exadmin.mpcr.misc.Settings;
import com.github.exadmin.mpcr.misc.StrUtils;
import com.github.exadmin.mpcr.misc.ThreadUtils;

import java.io.*;

public class EstablishConnectionThread extends MyRunnable {
    private static final String CMD_SHORT_FILENAME = ".\\_vpncli.cmd";

    private boolean printToConsole = false;

    public EstablishConnectionThread(FxSceneModel fxSceneModel, MyRunnable nextThreadToStart) {
        super(fxSceneModel, nextThreadToStart);
    }

    public EstablishConnectionThread(FxSceneModel fxSceneModel, MyRunnable nextThreadToStart, boolean printToConsole) {
        super(fxSceneModel, nextThreadToStart);
        this.printToConsole = printToConsole;
    }

    @Override
    protected void runSafe() {
        // shutdown video-camera - we do not need it anymore
        fxSceneModel.getVideoCapture().release();

        // create cmd-file to be executed
        File cmdFile = new File(CMD_SHORT_FILENAME);
        try (PrintWriter pw = new PrintWriter(cmdFile)) {
            if (Settings.isAutoStopEnabled()) {
                pw.println("taskkill /F /IM vpnagent.exe /IM vpnui.exe");
            }

            pw.println("\"" + Settings.getVpncliPath() + "\" -s < %1");
            pw.println("\"" + FileUtils.getFolderOnly(Settings.getVpncliPath()) + "\\vpnui.exe" + "\"");
        } catch (Exception ex) {
            printlnToFxConsole("Can't create or write to the command file " + cmdFile + ". Please check RW access. Terminating process, sorry. Exception is " + ex);
            return;
        }

        // create parameters file to be passed as argument into executable file
        File tmpCfgFile;
        try {
            tmpCfgFile = File.createTempFile("mpass", "");
        } catch (IOException ioe) {
            printlnToFxConsole("Can't create temp file in user's temp folder. Please check RW access. Terminating process, sorry. Exception is " + ioe);
            return;
        }

        try (PrintWriter pw = new PrintWriter(tmpCfgFile)) {
            pw.println("connect " + Settings.getVpnHost());
            pw.println(Settings.getNtLogin());
            char[] chars = Settings.getNtPassword(fxSceneModel.passPhraseForKeyStore.getValue()).toCharArray();
            for (char ch : chars) {
                pw.print(ch);
            }
            pw.println();
            pw.println(fxSceneModel.pinCode.getValue());
            pw.println("y");
            pw.println("exit");
        } catch (FileNotFoundException fnfe) {
            printlnToFxConsole("Can't file temp file in user's temp folder. Please check read access. Terminating process, sorry. Exception is " + fnfe);
            return;
        }

        if (fxSceneModel.isCLICallDisabled()) {
            printlnToFxConsole("CLI call is disabled. Stopping establishing process.");
            return;
        }

        Runtime rt = Runtime.getRuntime();
        String[] commands = {CMD_SHORT_FILENAME, tmpCfgFile.getAbsolutePath()};

        Process process;
        try {
            process = rt.exec(commands);
        } catch (IOException ioe) {
            printlnToFxConsole("Error while executing connection sub-process. Exception is "+ ioe);
            return;
        }

        try (InputStream stdout = process.getInputStream ();
             BufferedReader reader = new BufferedReader (new InputStreamReader(stdout))) {

            String line;
            while ((line = reader.readLine ()) != null) {
                line = line.trim();
                if (StrUtils.isStringEmpty(line, false)) continue;

                printlnToFxConsole(line);

                if (line.equals("goodbye...")) break;
            }
        } catch (IOException ioe) {
            printlnToFxConsole("Can't read from console stream. Abnormal run. Exception is " + ioe);
        }

        try {
            int attemptsToDeleteFile = 16;

            while (attemptsToDeleteFile > 0) {
                attemptsToDeleteFile--;

                boolean isDeleted = tmpCfgFile.delete();
                printlnToFxConsole("Temp file was " + (isDeleted ? "" : "not") + " deleted");

                if (isDeleted) {
                    printlnToFxConsole("Temp file was deleted successfully. You can close application now.");
                    break;
                }

                printlnToFxConsole("Waiting a little to repeat deletion attempt");
                ThreadUtils.sleep(attemptsToDeleteFile > 8 ? 500 : 1000); // let's wait more if first attempts are failed
            }

            if (attemptsToDeleteFile == 0) {
                printlnToFxConsole("ERROR: Can't delete temp file at "+ tmpCfgFile.getAbsolutePath() + ", please delete it manually! It contains NT Password in plain-text!!!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // todo: in case successful VPN connection establishing - we can add correctly recognized digits into the library (ala ML)
    }

    private void printlnToFxConsole(String message) {
        if (printToConsole) {
            System.out.println(message);
        }

        fxSceneModel.printToConsole(message);
    }
}
