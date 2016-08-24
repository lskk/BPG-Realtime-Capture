package org.pptik.bpgrealtimecapture.ftp;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import android.content.Context;
import android.util.Log;

/**
 * Created by hynra on 8/23/16.
 */

public class FtpHelper {
    private String TAG = this.getClass().getSimpleName();
    public FTPClient mFTPClient = null;


    public boolean ftpConnect(String host, String username, String password, int port) {
        try {
            mFTPClient = new FTPClient();
            mFTPClient.connect(host, port);

            if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {
                boolean status = mFTPClient.login(username, password);
                mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
                mFTPClient.enterLocalPassiveMode();

                return status;
            }
        } catch (Exception e) {
            Log.d(TAG, "Error: could not connect to host " + host);
        }

        return false;
    }

    public boolean ftpDisconnect() {
        try {
            mFTPClient.logout();
            mFTPClient.disconnect();
            return true;
        } catch (Exception e) {
            Log.d(TAG, "Error occurred while disconnecting from ftp server.");
        }

        return false;
    }

    public String ftpGetCurrentWorkingDirectory() {
        try {
            String workingDir = mFTPClient.printWorkingDirectory();
            return workingDir;
        } catch (Exception e) {
            Log.d(TAG, "Error: could not get current working directory.");
        }

        return null;
    }

    public boolean ftpChangeDirectory(String directory_path) {
        boolean isChanged = false;
        try {
            isChanged = mFTPClient.changeWorkingDirectory(directory_path);
            if(directory_path.equals(ftpGetCurrentWorkingDirectory())){
                isChanged = true;
            }
        } catch (Exception e) {
            Log.d(TAG, "Error: could not change directory to " + directory_path);
            isChanged = false;
        }

        return isChanged;
    }

    public String[] ftpPrintFilesList(String dir_path) {
        String[] fileList = null;
        try {
            FTPFile[] ftpFiles = mFTPClient.listFiles(dir_path);
            int length = ftpFiles.length;
            fileList = new String[length];
            for (int i = 0; i < length; i++) {
                String name = ftpFiles[i].getName();
                boolean isFile = ftpFiles[i].isFile();

                if (isFile) {
                    fileList[i] = "File : " + name;
                    Log.i(TAG, "File : " + name);
                } else {
                    fileList[i] = "Directory : " + name;
                    Log.i(TAG, "Directory : " + name);
                }
            }
            return fileList;
        } catch (Exception e) {
            e.printStackTrace();
            return fileList;
        }
    }

    public boolean ftpMakeDirectory(String new_dir_path) {
        try {
            boolean status = mFTPClient.makeDirectory(new_dir_path);
            return status;
        } catch (Exception e) {
            Log.d(TAG, "Error: could not create new directory named "
                    + new_dir_path);
        }

        return false;
    }

    public boolean ftpRemoveDirectory(String dir_path) {
        try {
            boolean status = mFTPClient.removeDirectory(dir_path);
            return status;
        } catch (Exception e) {
            Log.d(TAG, "Error: could not remove directory named " + dir_path);
        }

        return false;
    }


    public boolean ftpRemoveFile(String filePath) {
        try {
            boolean status = mFTPClient.deleteFile(filePath);
            return status;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean ftpRenameFile(String from, String to) {
        try {
            boolean status = mFTPClient.rename(from, to);
            return status;
        } catch (Exception e) {
            Log.d(TAG, "Could not rename file: " + from + " to: " + to);
        }

        return false;
    }

    public boolean ftpDownload(String srcFilePath, String desFilePath) {
        boolean status = false;
        try {
            FileOutputStream desFileStream = new FileOutputStream(desFilePath);
            ;
            status = mFTPClient.retrieveFile(srcFilePath, desFileStream);
            desFileStream.close();

            return status;
        } catch (Exception e) {
            Log.d(TAG, "download failed");
        }

        return status;
    }

    public boolean ftpUpload(String srcFilePath, String desFileName) {
        boolean status = false;
        try {
            FileInputStream srcFileStream = new FileInputStream(srcFilePath);
            status = mFTPClient.storeFile(desFileName, srcFileStream);
            srcFileStream.close();

            return status;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "upload failed: " + e);
        }

        return status;
    }
}