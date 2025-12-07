package peer.controllers;

import common.models.Message;
import common.utils.JSONUtils;
import common.utils.MD5Hash;
import peer.app.PeerApp;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;

public class PeerCLIController {
	public static String processCommand(String command) throws Exception {
		if(PeerCommands.List.getMatcher(command).matches()){
			return handleListFiles();
		}
		else if(PeerCommands.END.getMatcher(command).matches()){
			return endProgram();
		}
		else if(PeerCommands.Download.getMatcher(command).matches()){
			return handleDownload(command);
		}
		else {
			return PeerCommands.invalidCommand;
		}
	}

	private static String handleListFiles() {
		File folder = new File(PeerApp.getSharedFolderPath());
		File[] listOfFiles = folder.listFiles();
		ArrayList<File> files = new ArrayList<>();
		if(listOfFiles.length == 0){
			return "Repository is empty.";
		}
		for (File file : listOfFiles) {
			if (file.isFile()) {
				files.add(file);
			}
		}
		files.sort(Comparator.comparing(File::getName));
		StringBuilder sb = new StringBuilder();
		for (File file : files) {
			sb.append(file.getName()).append(" ").append(MD5Hash.HashFile(file.getPath())).append("\n");
		}
		return sb.toString();
	}

	private static String handleDownload(String command) throws Exception {
		Matcher matcher = PeerCommands.Download.getMatcher(command);
		boolean a = matcher.matches();
		String fileName = matcher.group(1);
		File folder = new File(PeerApp.getSharedFolderPath());
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			if (file.getName().equals(fileName)) {
				return "You already have the file!";
			}
		}
		// Send file request to tracker
		Message fileRequestMessage = P2TConnectionController.sendFileRequest(PeerApp.getP2TConnection(),fileName);
		// Get peer info and file hash
		if(fileRequestMessage.getFromBody("response").equals("error")){
			if(fileRequestMessage.getFromBody("error").equals("not_found")){
				return "No peer has the file!";
			}
			else return "Multiple hashes found!";
		}
		// Request file from peer
		String peerAddress = (String) fileRequestMessage.getFromBody("peer_have");
		int peerPort = fileRequestMessage.getIntFromBody("peer_port");
		String expectedHash = (String) fileRequestMessage.getFromBody("md5");
		try {
			Socket peerSocket = new Socket(peerAddress, peerPort);

			// Create download request message
			HashMap<String, Object> requestMap = new HashMap<>();
			requestMap.put("name", fileName);
			requestMap.put("md5", expectedHash);
			requestMap.put("receiver_ip", PeerApp.getPeerIP());
			requestMap.put("receiver_port", PeerApp.getPeerPort());
			Message downloadRequest = new Message(requestMap, Message.Type.download_request);

			// Send request
			DataOutputStream out = new DataOutputStream(peerSocket.getOutputStream());
			out.writeUTF(JSONUtils.toJson(downloadRequest));

			// Receive file
			File receivedFile = new File(PeerApp.getSharedFolderPath() + File.separator + fileName);
			try (BufferedInputStream in = new BufferedInputStream(peerSocket.getInputStream());
				 FileOutputStream fileOut = new FileOutputStream(receivedFile)) {

				byte[] buffer = new byte[8192];
				int bytesRead;
				while ((bytesRead = in.read(buffer)) != -1) {
					fileOut.write(buffer, 0, bytesRead);
				}
			}

			// Verify hash
			String actualHash = MD5Hash.HashFile(receivedFile.getAbsolutePath());
			if (!actualHash.equals(expectedHash)) {
				receivedFile.delete();
				return "The file has been downloaded from peer but is corrupted!";
			}

			PeerApp.addReceivedFile(peerAddress + ":" + peerPort , fileName + " " + actualHash);

			return "File downloaded successfully: " + fileName;
		} catch (IOException e) {
			e.printStackTrace();
			return "Download failed: " + e.getMessage();
		}
	}

	public static String endProgram() {
		PeerApp.endAll();
		return "";
	}
}
