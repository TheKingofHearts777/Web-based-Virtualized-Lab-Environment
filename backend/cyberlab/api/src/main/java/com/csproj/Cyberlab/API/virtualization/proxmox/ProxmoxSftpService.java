package com.csproj.Cyberlab.API.virtualization.proxmox;

import com.csproj.Cyberlab.API.exceptions.FileUploadException;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Properties;

//--------------------------------------------------
// Static functionality for Proxmox sftp uploads
//--------------------------------------------------
@Service
@Slf4j
public class ProxmoxSftpService {
    private final String SFTP_HOST;
    private final int SFTP_PORT;
    private final String SFTP_USER;
    private final String SFTP_PASSWORD;
    private final String SFTP_REMOTE_DIR;
    private final long MAX_FILE_SIZE;

   public ProxmoxSftpService(Environment env) {
       this.SFTP_HOST = env.getProperty("sftp.api.hostname");
       this.SFTP_PORT = Integer.parseInt(env.getProperty("sftp.port", "22"));
       this.SFTP_USER = env.getProperty("sftp.username");
       this.SFTP_PASSWORD = env.getProperty("sftp.password");
       this.SFTP_REMOTE_DIR = env.getProperty("sftp.path");
       this.MAX_FILE_SIZE = Long.parseLong(env.getProperty("file.upload.max-size", "10737418240")); // Default 10GB

       if (SFTP_HOST == null || SFTP_HOST.isEmpty() ||
               SFTP_USER == null || SFTP_USER.isEmpty() ||
               SFTP_PASSWORD == null || SFTP_PASSWORD.isEmpty() ||
               SFTP_REMOTE_DIR == null || SFTP_REMOTE_DIR.isEmpty())
       {
           throw new IllegalArgumentException("Missing or null SFTP connection vars");
       }
   }

    /**
     * Handle SFTP transfer logic for a VDI file
     * @param inputStream Input stream from file uploaded through HTTP request MUST ONLY contain a VDI file
     * @param filename File name to be stored temporarily on SFTP server location
     */
    public void uploadVdi(InputStream inputStream, String filename) throws FileUploadException {
        log.info("Connecting to SFTP: {}@{}:{}", SFTP_USER, SFTP_HOST, SFTP_PORT);

        try {
            Session session = null;
            ChannelSftp channelSftp = null;
            byte[] header = new byte[0x178];
            int bytesRead = inputStream.read(header);

            if (bytesRead < 10 || !isValidHeader(header)) {
                log.error("Invalid VDI file: File type != vdi or virtual disk size > {}GB", this.MAX_FILE_SIZE / 1024 / 1024 / 1024);
                throw new FileUploadException("Invalid VDI file: File type != vdi or virtual disk size > " + this.MAX_FILE_SIZE / 1024 / 1024 / 1024 + "GB");  // Will want to change to custom exception
            }

            InputStream fullStream = new SequenceInputStream(new ByteArrayInputStream(header), inputStream);

            JSch jsch = new JSch();
            session = jsch.getSession(SFTP_USER, SFTP_HOST, SFTP_PORT);
            session.setPassword(SFTP_PASSWORD);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            channelSftp.put(fullStream, SFTP_REMOTE_DIR + filename);
        }
        catch (Exception e) {
            log.warn("Encountered error when uploading file: " + e.getMessage());
        }

        log.info("File successfully uploaded to SFTP.");
    }
    /**
     * Run commands on uploaded file to import to logical disk and remove from sftp temp directory
     * @param fileName File name to run actions on
     * @param vmId VM ID to import disk to
     * @throws FileUploadException Exception if command does not succeed
     */
    public void executeRemoteCommand(String fileName, int vmId) throws FileUploadException {
        // Command to import disk to logical storage medium, then remove from temp folder
        String command = "qm importdisk " + vmId + " " + SFTP_REMOTE_DIR + fileName + " local-lvm; rm " + SFTP_REMOTE_DIR + fileName;
        Session session = null;
        ChannelExec channelExec = null;
        StringBuilder outputBuffer = new StringBuilder();

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(SFTP_USER, SFTP_HOST, SFTP_PORT);
            session.setPassword(SFTP_PASSWORD);

            // Bypass host key checking (optional, for testing purposes)
            session.setConfig("StrictHostKeyChecking", "no");

            session.connect();
            log.info("SSH Connection Established to {}", SFTP_HOST);

            // Create an execution channel
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            channelExec.setInputStream(null);
            channelExec.setErrStream(System.err);

            // Capture the output
            InputStream inputStream = channelExec.getInputStream();
            channelExec.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                outputBuffer.append(line).append("\n");
            }

            while (!channelExec.isClosed()) {
                Thread.sleep(1000); // Small delay to avoid busy-waiting
            }

            int exitStatus = channelExec.getExitStatus();
            if (exitStatus != 0) {
                log.error("Command failed with exit status: {}", exitStatus);
                throw new FileUploadException("Command failed with exit status" + exitStatus);
            }

            // Log execution status
            log.info("Command executed: {}", command);
            log.info("Command Output: {}", outputBuffer.toString().split("\n")[outputBuffer.toString().split("\n").length - 1]);

        } catch (Exception e) {
            log.error("Error executing remote command: {}", e.getMessage());
            throw new FileUploadException(e.getMessage());
        } finally {
            if (channelExec != null) channelExec.disconnect();
            if (session != null) session.disconnect();
        }
    }

    /**
     * Checks header hex values of input file to verify file type
     * @param header byte array containing header of uploaded file (first 0x170 bytes)
     * @return boolean of header validity as vdi file type of less than maximum virtual disk size
     */
    private boolean isValidHeader(byte[] header) {
        byte[] expectedMagicBytes = { 0x3C, 0x3C, 0x3C, 0x20, 0x4F, 0x72, 0x61, 0x63, 0x6C, 0x65 }; // Expected VDI signature

        // Validate file header
        for (int i = 0; i < 10; i++) {
            if (expectedMagicBytes[i] != header[i]) {
                return false;
            }
        }

        // Check virtual disk size in header at byte offset 0x170
        long fileSizeLong = littleEndianBytesToLong(header, 0x170);
        if (fileSizeLong > this.MAX_FILE_SIZE) {
            return false;
        }

        return true;
    }

    /**
     * Converts an 8-byte little-endian byte array segment into a long.
     * @param data   The byte array containing the little-endian integer.
     * @param offset The starting position of the 8-byte value in the array.
     * @return The corresponding long value.
     */
    private long littleEndianBytesToLong(byte[] data, int offset) {
        return ((long) data[offset] & 0xFF) |
                (((long) data[offset + 1] & 0xFF) << 8) |
                (((long) data[offset + 2] & 0xFF) << 16) |
                (((long) data[offset + 3] & 0xFF) << 24) |
                (((long) data[offset + 4] & 0xFF) << 32) |
                (((long) data[offset + 5] & 0xFF) << 40) |
                (((long) data[offset + 6] & 0xFF) << 48) |
                (((long) data[offset + 7] & 0xFF) << 56);
    }
}
