package me.libraryaddict.build.types;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilFile;
import me.libraryaddict.core.utils.UtilString;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Vector;

public class RemoteFileManager {
    /**
     * Yes I'm aware this wasn't good practice
     */
    private static byte[] _sshIdentity;
    private String _ip;
    private MapInfo _mapInfo;

    public RemoteFileManager() {
    }

    public RemoteFileManager(String ip) {
        _ip = ip;
    }

    public RemoteFileManager(String ip, MapInfo mapInfo) {
        _ip = ip;
        _mapInfo = mapInfo;
    }

    public void addBackup(String date, String version) throws Exception {
        assert (date.length() == 10);

        UtilString.log("Backing up " + getInfo().getUUID().toString() + " to " + date + " with version " + version);

        getAmazon()
                .putObject("redwarfare-maps", date + "/" + getInfo().getUUID().toString() + ".zip", getInfo().getZip());

        getInfo().setBackedUp(date, version);
    }

    public void copyFileToLocal() throws Exception {
        if (getInfo().getIPLoc().equals(_ip) && getInfo().getFileLoc().equals(getInfo().getZip().getAbsolutePath()))
            throw new IllegalStateException("Trying to grab file to copy to the same location");

        UtilFile.delete(getInfo().getZip());

        if (getInfo().getIPLoc().equals(_ip)) {
            System.out.println(
                    "Copying file from " + getInfo().getFileLoc() + " to " + getInfo().getZip().getAbsolutePath());
            grabLocalFile();
        } else {
            System.out.println(
                    "Downloading file from " + getInfo().getIPLoc() + " " + getInfo().getFileLoc() + " to " + getInfo()
                            .getZip().getAbsolutePath());
            grabRemoteFile();
        }

        if (!getInfo().getZip().exists())
            throw new IllegalStateException("Failed to copy over map");

        deleteFile();

        getInfo().setLocation(_ip, getInfo().getZip().getAbsolutePath());
        getInfo().save();
    }

    public void deleteFile() throws Exception {
        if (getInfo().getIPLoc().equals(_ip)) {
            System.out.println("Deleting file at " + getInfo().getFileLoc());
            deleteLocalFile();
        } else {
            System.out.println("Deleting file on " + getInfo().getIPLoc() + " at " + getInfo().getFileLoc());
            deleteRemoteFile(getInfo().getIPLoc(), getInfo().getFileLoc());
        }

        getInfo().setLocation(null, null);
    }

    public void deleteLocalFile() {
        UtilString.log("Deleting local file " + getInfo().getFileLoc());

        UtilFile.delete(new File(getInfo().getFileLoc()));
    }

    public void deleteRemoteFile(String ip, String path) throws Exception {
        UtilString.log("Deleting remote file at " + ip + " at " + path);

        init();

        JSch ssh = new JSch();

        JSch.setConfig("StrictHostKeyChecking", "no");

        Session session = null;

        try {
            session = ssh.getSession("libraryaddict", ip, 22);
            ssh.addIdentity("libraryaddict", _sshIdentity, null, null);

            session.connect();
            Channel channel = null;
            try {
                channel = session.openChannel("sftp");
                channel.connect();

                ChannelSftp sftp = (ChannelSftp) channel;

                sftp.rm(path);
            }
            finally {
                if (channel != null) {
                    channel.disconnect();
                }
            }
        }
        catch (Throwable ex) {
            System.out.println("Um yeah");
            ex.printStackTrace();
        }
        finally {
            session.disconnect();
        }
    }

    public void exportMapToSite(String localFile, String ip, String user, String password,
            String remoteFile) throws Exception {
        init();

        JSch ssh = new JSch();

        Session session = null;

        try {
            session = ssh.getSession(user, ip, 22);
            session.setPassword(password);

            session.connect();

            Channel channel = null;

            try {
                channel = session.openChannel("sftp");
                channel.connect();

                ChannelSftp sftp = (ChannelSftp) channel;

                System.out.println("Putting " + localFile + " to " + remoteFile);
                sftp.put(localFile, remoteFile, ChannelSftp.OVERWRITE);
            }
            finally {
                if (channel != null) {
                    channel.disconnect();
                    channel = null;
                }
            }

            try {
                channel = session.openChannel("exec");

                ChannelExec exec = (ChannelExec) channel;

                BufferedReader in = new BufferedReader(new InputStreamReader(exec.getInputStream()));
                exec.setCommand("find /home/files/web/download/ -type f -mmin +60 -delete");
                exec.connect();

                String msg = null;

                while ((msg = in.readLine()) != null) {
                    System.out.println(msg);
                }
            }
            finally {
                if (channel != null) {
                    channel.disconnect();
                }
            }
        }
        finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }

    private AmazonS3Client getAmazon() {
        AmazonS3Client client = new AmazonS3Client(new AWSCredentials() {

            @Override
            public String getAWSAccessKeyId() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getAWSSecretKey() {
                throw new UnsupportedOperationException();
            }
        });

        return client;
    }

    private MapInfo getInfo() {
        return _mapInfo;
    }

    public void grabBackup() throws Exception {
        grabBackup(getInfo().getBackup());
    }

    public void grabBackup(String backupLocation) throws Exception {
        UtilString.log("Grabbing backup from " + backupLocation + " to " + getInfo().getFileLoc());

        UtilFile.delete(getInfo().getZip());

        InputStream stream = getAmazon()
                .getObject("redwarfare-maps", backupLocation + "/" + getInfo().getUUID().toString() + ".zip")
                .getObjectContent();

        FileOutputStream output = new FileOutputStream(getInfo().getZip());

        IOUtils.copy(stream, output);

        output.close();

        stream.close();

        if (!getInfo().getZip().exists())
            throw new IllegalStateException("Failed to download map");

        getInfo().setLocation(_ip, getInfo().getZip().getAbsolutePath());
    }

    private void grabLocalFile() {
        UtilString.log("Copying local file from  " + getInfo().getFileLoc() + " to " + getInfo().getZip()
                .getAbsolutePath());

        UtilFile.copyFile(new File(getInfo().getFileLoc()), getInfo().getZip());
    }

    private void grabRemoteFile() throws Exception {
        init();

        JSch ssh = new JSch();

        JSch.setConfig("StrictHostKeyChecking", "no");

        UtilString.log("Copying remote file from  " + getInfo().getIPLoc() + " - " + getInfo()
                .getFileLoc() + " to " + getInfo().getZip().getAbsolutePath() + " " + getInfo().getZip().length());

        Session session = null;

        try {
            session = ssh.getSession("libraryaddict", getInfo().getIPLoc(), 22);
            ssh.addIdentity("libraryaddict", _sshIdentity, null, null);

            session.connect();
            Channel channel = null;

            try {
                channel = session.openChannel("sftp");
                channel.connect();

                ChannelSftp sftp = (ChannelSftp) channel;

                sftp.get(getInfo().getFileLoc(), getInfo().getZip().getAbsolutePath());
            }
            catch (Exception ex) {

            }
            finally {
                if (channel != null) {
                    channel.disconnect();
                }
            }
        }
        finally {
            session.disconnect();
        }
    }

    private void init() {
        if (_sshIdentity != null)
            return;

        JSch.setConfig("StrictHostKeyChecking", "no");

        try (InputStream stream = getClass().getResourceAsStream("/build_ssh.ppk")) {
            _sshIdentity = new byte[stream.available()];
            stream.read(_sshIdentity);
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }
    }

    public Vector<LsEntry> listFiles(String ip, String path) throws Exception {
        init();

        JSch ssh = new JSch();

        JSch.setConfig("StrictHostKeyChecking", "no");

        Session session = null;
        Vector<LsEntry> list = null;

        try {
            session = ssh.getSession("libraryaddict", ip, 22);
            ssh.addIdentity("libraryaddict", _sshIdentity, null, null);

            session.connect();

            Channel channel = null;

            try {
                channel = session.openChannel("sftp");
                channel.connect();

                ChannelSftp sftp = (ChannelSftp) channel;

                list = sftp.ls(path);
            }
            finally {
                if (channel != null) {
                    channel.disconnect();
                }
            }
        }
        finally {
            session.disconnect();
        }

        return list;
    }

    public void putFile(File file, String ip, String dest) throws Exception {
        dest = dest.trim();

        init();

        JSch ssh = new JSch();

        JSch.setConfig("StrictHostKeyChecking", "no");

        Session session = null;

        try {
            session = ssh.getSession("libraryaddict", ip, 22);
            ssh.addIdentity("libraryaddict", _sshIdentity, null, null);

            session.connect();
            Channel channel = null;

            try {
                channel = session.openChannel("sftp");
                channel.connect();

                ChannelSftp sftp = (ChannelSftp) channel;
                /* if (!dest.endsWith("/") && dest.contains("/"))
                {
                    String folder = dest.substring(0, dest.lastIndexOf("/"));

                    if (!sftp.ex)
                    sftp.mkdir(dest.substring(dest.lastIndexOf("/")));
                }*/

                FileInputStream stream = new FileInputStream(file);

                sftp.put(stream, dest);

                stream.close();
            }
            finally {
                if (channel != null) {
                    channel.disconnect();
                }
            }
        }
        finally {
            session.disconnect();
        }
    }
}
