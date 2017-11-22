package de.flashheart.ocfflag.misc;

import de.flashheart.ocfflag.Main;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * FTP-Utility, basierend auf Apache FTPClient:
 * {@link "http://commons.apache.org/net/apidocs/org/apache/commons/net/ftp/FTPClient.html"}
 */
public class FTPWrapper {
    /**
     * FTP-Dateienliste.
     *
     * @return String-Array der Dateinamen auf dem FTP-Server
     */
    public static String[] list(String host, int port, String usr, String pwd) throws IOException {
        FTPClient ftpClient = new FTPClient();
        String[] filenameList;

        try {
            ftpClient.connect(host, port);
            ftpClient.login(usr, pwd);
            filenameList = ftpClient.listNames();
            ftpClient.logout();
        } finally {
            ftpClient.disconnect();
        }

        return filenameList;
    }

    /**
     * FTP-Client-Download.
     *
     * @return true falls ok
     */
    public static boolean download(String localResultFile, String remoteSourceFile,
                                   String host, int port, String usr, String pwd, boolean showMessages) throws IOException {
        FTPClient ftpClient = new FTPClient();
        FileOutputStream fos = null;
        boolean resultOk = true;
        Logger logger = Logger.getLogger(FTPWrapper.class);
        logger.setLevel(Main.getLogLevel());
        try {
            ftpClient.connect(host, port);
            if (showMessages) {
                logger.debug(ftpClient.getReplyString());
            }
            resultOk &= ftpClient.login(usr, pwd);
            if (showMessages) {
                logger.debug(ftpClient.getReplyString());
            }
            fos = new FileOutputStream(localResultFile);
            resultOk &= ftpClient.retrieveFile(remoteSourceFile, fos);
            if (showMessages) {
                logger.debug(ftpClient.getReplyString());
            }
            resultOk &= ftpClient.logout();
            if (showMessages) {
                logger.debug(ftpClient.getReplyString());
            }
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {/* nothing to do */}
            ftpClient.disconnect();
        }

        return resultOk;
    }

    /**
     * FTP-Client-Upload.
     *
     * @return true falls ok
     */
    public static boolean upload(String content) throws IOException {

        FTPClient ftpClient = new FTPClient();

        if (!Main.getConfigs().isFTPComplete()) return false;

        String host = Main.getConfigs().get(Configs.FTPHOST);
        int port = Integer.parseInt(Main.getConfigs().get(Configs.FTPPORT));
        String usr = Main.getConfigs().get(Configs.FTPUSER);
        String pwd = Main.getConfigs().get(Configs.FTPPWD);
        String remotepath = Main.getConfigs().get(Configs.FTPREMOTEPATH);
        String uuid = Main.getConfigs().get(Configs.MYUUID);

        String remoteFile = remotepath + "/active/" + uuid + ".php";

        boolean resultOk = true;
        Logger logger = Logger.getLogger(FTPWrapper.class);
        logger.setLevel(Main.getLogLevel());

        File tempPHPFile = File.createTempFile("ocfflag", ".php");
        tempPHPFile.deleteOnExit();
        FileUtils.writeStringToFile(tempPHPFile, content, "UTF-8");

        FileInputStream fis = null;


        try {
            ftpClient.connect(host, port);

            logger.debug(ftpClient.getReplyString());

            resultOk &= ftpClient.login(usr, pwd);

            logger.debug(ftpClient.getReplyString());

            fis = new FileInputStream(tempPHPFile);
            resultOk &= ftpClient.storeFile(remoteFile, fis);

            logger.debug(ftpClient.getReplyString());

            resultOk &= ftpClient.logout();

            logger.debug(ftpClient.getReplyString());

        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {/* nothing to do */}
            ftpClient.disconnect();
        }

        return resultOk;
    }

    /**
     * Richtet das FTP Verzeichnis ein.
     * Verschiebt evtl. kaputte oder abgeschlossene PHP Statistiken in das Archivverzeichnis.
     * Dabei benennt es die Datei um.
     *
     * @return true falls ok
     */
    public static boolean initFTPDir() throws IOException {
        FTPClient ftpClient = new FTPClient();

        if (!Main.getConfigs().isFTPComplete()) return false;

        String host = Main.getConfigs().get(Configs.FTPHOST);
        int port = Integer.parseInt(Main.getConfigs().get(Configs.FTPPORT));
        String usr = Main.getConfigs().get(Configs.FTPUSER);
        String pwd = Main.getConfigs().get(Configs.FTPPWD);


        boolean resultOk = true;
        Logger logger = Logger.getLogger(FTPWrapper.class);
        logger.setLevel(Main.getLogLevel());
        try {
            ftpClient.connect(host, port);

            logger.debug(ftpClient.getReplyString());

            resultOk &= ftpClient.login(usr, pwd);

            logger.debug(ftpClient.getReplyString());

            if (resultOk) {
                DateTime now = new DateTime();
                String uuid = Main.getConfigs().get(Configs.MYUUID);
                String remotepath = Main.getConfigs().get(Configs.FTPREMOTEPATH);
                String remoteFile = remotepath + "/active/" + uuid + ".php";
                String localFile = Tools.getWorkingPath() + File.separator + uuid + ".php";
                String archivepath = remotepath + "/archive";
                String archivefile = archivepath + "/" + now.toString("yyyyMMddHHmmss") + "-" + uuid + ".php";

                // archive Verzeichnis erstellen, wenn nötig
                resultOk &= ftpClient.makeDirectory(archivepath);

                // Gibts noch eine aktive Statistik Datei ?
                FileOutputStream fos = new FileOutputStream(localFile);
                boolean remoteFileStillExisting = ftpClient.retrieveFile(remoteFile, fos);

                if (remoteFileStillExisting) {
                    fos.close();
                    ftpClient.deleteFile(remoteFile);

                    // ins Archiv verschieben
                    FileInputStream fis = new FileInputStream(localFile);
                    resultOk &= ftpClient.storeFile(archivefile, fis);

                    logger.debug(archivefile + ": " + ftpClient.getReplyString());

                    resultOk &= ftpClient.logout();
                    logger.debug(ftpClient.getReplyString());
                }
            }
        } finally {
            ftpClient.disconnect();
        }

        return resultOk;
    }
}