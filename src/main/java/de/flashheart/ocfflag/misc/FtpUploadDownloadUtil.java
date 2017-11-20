package de.flashheart.ocfflag.misc;

import de.flashheart.ocfflag.Main;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * FTP-Utility, basierend auf Apache FTPClient:
 * {@link "http://commons.apache.org/net/apidocs/org/apache/commons/net/ftp/FTPClient.html"}
 */
public class FtpUploadDownloadUtil {
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
        Logger logger = Logger.getLogger(FtpUploadDownloadUtil.class);
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
    public static boolean upload(String localSourceFile, String remotePath, String remoteResultFile,
                                 String host, int port, String usr, String pwd, boolean showMessages) throws IOException {

        FTPClient ftpClient = new FTPClient();
        FileInputStream fis = null;
        boolean resultOk = true;
        Logger logger = Logger.getLogger(FtpUploadDownloadUtil.class);
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

            fis = new FileInputStream(localSourceFile);
            resultOk &= ftpClient.storeFile(remotePath + "/" + remoteResultFile, fis);
            if (showMessages) {
                logger.debug(ftpClient.getReplyString());
            }
            resultOk &= ftpClient.logout();
            if (showMessages) {
                logger.debug(ftpClient.getReplyString());
            }
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
     * Verschiebt evtl. kaputte PHP Statistiken in das Archivverzeichnis.
     * Dabei benennt es die Datei um.
     *
     * @return true falls ok
     */
    public static boolean initFTPDir(String host, int port, String usr, String pwd) throws IOException {
        FTPClient ftpClient = new FTPClient();

        boolean resultOk = true;
        Logger logger = Logger.getLogger(FtpUploadDownloadUtil.class);
        logger.setLevel(Main.getLogLevel());
        try {
            ftpClient.connect(host, port);

            logger.debug(ftpClient.getReplyString());

            resultOk &= ftpClient.login(usr, pwd);

            logger.debug(ftpClient.getReplyString());

            if (resultOk) {
                String uuid = Main.getConfigs().get(Configs.MYUUID);
                String remotepath = Main.getConfigs().get(Configs.FTPREMOTEPATH);
                String remoteFile = remotepath + "/" + uuid + ".php";
                String localFile = Tools.getWorkingPath() + File.separator + uuid + ".php";
                String archivepath = remotepath + "/archive";

                // archive Verzeichnis erstellen, wenn nötig
                resultOk &= ftpClient.makeDirectory(archivepath);

                // Gibts noch eine aktive Statistik Datei ?
                FileOutputStream fos = new FileOutputStream(localFile);
                boolean remoteFileFixed = ftpClient.retrieveFile(remoteFile, fos);

                if (remoteFileFixed) {
                    String modificationTime = ftpClient.getModificationTime(remoteFile);
                    fos.close();

                    ftpClient.deleteFile(remoteFile);

                    FileInputStream fis = new FileInputStream(localFile);

                    hier gehts weiter
                    resultOk &= ftpClient.storeFile(archivepath + "/" + uuid + ".php", fis);

                    logger.debug(ftpClient.getReplyString());

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