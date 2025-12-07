package tracker.controllers;

import common.utils.FileUtils;
import tracker.app.PeerConnectionThread;
import tracker.app.TrackerApp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class TrackerCLIController {
	public static String processCommand(String command) throws IOException {
		if(TrackerCommands.RESET_CONNECTIONS.getMatcher(command).matches()) {
			return resetConnections();
		}
		else if(TrackerCommands.REFRESH_FILES.getMatcher(command).matches()) {
			return refreshFiles();
		}
		else if(TrackerCommands.LIST_PEERS.getMatcher(command).matches()) {
			return listPeers();
		}
		else if(TrackerCommands.LIST_FILES.getMatcher(command).matches()) {
			return listFiles(command);
		}
		else if(TrackerCommands.GET_SENDS.getMatcher(command).matches()) {
			return getSends(command);
		}
		else if(TrackerCommands.GET_RECEIVES.getMatcher(command).matches()) {
			return getReceives(command);
		}
		else if(TrackerCommands.END.getMatcher(command).matches()) {
			return endProgram();
		}
		else {
			return TrackerCommands.invalidCommand;
		}
	}

	private static String getReceives(String command) {
		Matcher matcher = TrackerCommands.GET_RECEIVES.getMatcher(command);
		if(!matcher.matches()) {
			return TrackerCommands.invalidCommand;
		}
		String ipPort = matcher.group(1);
		String[] split = ipPort.split(":");
		String ip = split[0];
		int port = Integer.parseInt(split[1]);
		PeerConnectionThread connection = TrackerApp.getConnectionByIpPort(ip, port);
		if(connection == null) {
			return "Peer not found.";
		}
		Map<String, List<String>> receives = TrackerConnectionController.getReceives(connection);
		if(receives.isEmpty()) {
			return "No files received by " + ipPort;
		}
		List<String> resultLines = new ArrayList<>();

		receives.forEach((senderIpPort, fileEntries) -> {
			for (String fileEntry : fileEntries) {
				String[] parts = fileEntry.split(" ");
				if (parts.length >= 2) {
					String fileName = parts[0];
					String md5Hash = parts[1];
					resultLines.add(String.format("%s %s - %s", fileName, md5Hash, senderIpPort));
				}
			}
		});

		// Sort first by filename, then by sender IP:port
		resultLines.sort((line1, line2) -> {
			String[] parts1 = line1.split(" ", 2); // Split only on first space
			String[] parts2 = line2.split(" ", 2);

			// Compare filenames first
			int fileNameCompare = parts1[0].compareTo(parts2[0]);
			if (fileNameCompare != 0) {
				return fileNameCompare;
			}

			// If filenames are equal, compare the entire line (which includes the sender IP:port)
			return line1.compareTo(line2);
		});

		// Join the sorted lines with newlines
		return String.join("\n", resultLines);
	}

	private static String getSends(String command) {
		Matcher matcher = TrackerCommands.GET_SENDS.getMatcher(command);
		if(!matcher.matches()) {
			return TrackerCommands.invalidCommand;
		}
		String ipPort = matcher.group(1);
		String[] split = ipPort.split(":");
		String ip = split[0];
		int port = Integer.parseInt(split[1]);
		PeerConnectionThread connection = TrackerApp.getConnectionByIpPort(ip, port);
		if(connection == null) {
			return "Peer not found.";
		}
		Map<String, List<String>> sends = TrackerConnectionController.getSends(connection);
		if(sends.isEmpty()) {
			return "No files sent by " + ipPort;
		}
		List<String> resultLines = new ArrayList<>();

		sends.forEach((senderIpPort, fileEntries) -> {
			for (String fileEntry : fileEntries) {
				String[] parts = fileEntry.split(" ");
				if (parts.length >= 2) {
					String fileName = parts[0];
					String md5Hash = parts[1];
					resultLines.add(String.format("%s %s - %s", fileName, md5Hash, senderIpPort));
				}
			}
		});

		// Sort first by filename, then by sender IP:port
		resultLines.sort((line1, line2) -> {
			String[] parts1 = line1.split(" ", 2); // Split only on first space
			String[] parts2 = line2.split(" ", 2);

			// Compare filenames first
			int fileNameCompare = parts1[0].compareTo(parts2[0]);
			if (fileNameCompare != 0) {
				return fileNameCompare;
			}

			// If filenames are equal, compare the entire line (which includes the sender IP:port)
			return line1.compareTo(line2);
		});

		// Join the sorted lines with newlines
		return String.join("\n", resultLines);
	}

	private static String listFiles(String command) {
		Matcher matcher = TrackerCommands.LIST_FILES.getMatcher(command);
		if(!matcher.matches()) {
			return TrackerCommands.invalidCommand;
		}
		String ipPort = matcher.group(1);
		String[] split = ipPort.split(":");
		String ip = split[0];
		int port = Integer.parseInt(split[1]);
		PeerConnectionThread connection = TrackerApp.getConnectionByIpPort(ip, port);
		if(connection == null) {
			return "Peer not found.";
		}
		Map<String , String> files = connection.getFileAndHashes();
		return FileUtils.getSortedFileList(files);
	}

	private static String listPeers() {
		StringBuilder result = new StringBuilder();
		if(TrackerApp.getConnections().isEmpty()) {
			return "No peers connected.";
		}
		for (PeerConnectionThread connection : TrackerApp.getConnections()) {
			result.append(connection.getOtherSideIP()).append(":").append(connection.getOtherSidePort()).append("\n");
		}
		return result.toString();
	}

	private static String resetConnections() throws IOException {
		ArrayList<PeerConnectionThread> connectionsCopy = new ArrayList<>(TrackerApp.getConnections());
		StringBuilder result = new StringBuilder();
		int failedResets = 0;

		for (PeerConnectionThread connection : connectionsCopy) {
			try {
				// Test if connection is responsive by refreshing status
				connection.refreshStatus();

				// If we get here, connection is responsive - refresh its file list
				connection.refreshFileList();

			} catch (IOException e) {
				// Connection is unresponsive - close and remove it
				result.append("Connection to ")
						.append(connection.getOtherSideIP())
						.append(":")
						.append(connection.getOtherSidePort())
						.append(" was unresponsive and has been removed\n");
				connection.end();
				TrackerApp.removePeerConnection(connection);
				failedResets++;
			}
		}
		return "";
	}

	private static String refreshFiles() throws IOException {
		StringBuilder result = new StringBuilder();
		for (PeerConnectionThread connection : TrackerApp.getConnections()) {
			connection.refreshFileList();
		}
		return "";
	}

	private static String endProgram() {
		TrackerApp.endAll();
		return "";
	}
}
