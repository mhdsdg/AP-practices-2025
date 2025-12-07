package peer.app;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class PeerApp {
	public static final int TIMEOUT_MILLIS = 500;

	private static String peerIP;
	private static int peerPort;
	private static String trackerIP;
	private static int trackerPort;
	private static String sharedFolderPath;

	// File tracking
	private static Map<String, List<String>> sentFiles = new HashMap<>();
	private static Map<String, List<String>> receivedFiles = new HashMap<>();

	// Thread management
	private static P2TConnectionThread trackerConnectionThread;
	private static P2PListenerThread peerListenerThread;
	private static Set<TorrentP2PThread> torrentThreads = new HashSet<>();
	private static boolean exitFlag = false;

	public static boolean isEnded() {
		return exitFlag;
	}

	public static void initFromArgs(String[] args) throws Exception {
		// 1. Parse self address (ip:port)
		String[] peerAddress = args[0].split(":");
		if (peerAddress.length != 2) {
			throw new IllegalArgumentException("Invalid peer address format. Expected <ip:port>");
		}
		peerIP = peerAddress[0];
		peerPort = Integer.parseInt(peerAddress[1]);

		// 2. Parse tracker address (ip:port)
		String[] trackerAddress = args[1].split(":");
		if (trackerAddress.length != 2) {
			throw new IllegalArgumentException("Invalid tracker address format. Expected <ip:port>");
		}
		trackerIP = trackerAddress[0];
		trackerPort = Integer.parseInt(trackerAddress[1]);

		// 3. Set shared folder path
		sharedFolderPath = args[2];

		// Create shared folder if it doesn't exist
		java.io.File folder = new java.io.File(sharedFolderPath);
		if (!folder.exists()) {
			if (!folder.mkdirs()) {
				throw new Exception("Could not create shared folder: " + sharedFolderPath);
			}
		}

		// 4. Create tracker connection thread
		trackerConnectionThread = new P2TConnectionThread(new Socket(trackerIP, trackerPort));

		// 5. Create peer listener thread
		peerListenerThread = new P2PListenerThread(peerPort);
	}

	public static void endAll() {
		exitFlag = true;

		// 1. End tracker connection
		if (trackerConnectionThread != null) {
			trackerConnectionThread.end();
		}

		// 2. End all torrent threads
		synchronized (torrentThreads) {
			for (TorrentP2PThread thread : torrentThreads) {
				thread.end();
			}
			torrentThreads.clear();
		}
		// 3. Clear file lists
		sentFiles.clear();
		receivedFiles.clear();
	}

	public static void connectTracker() {
		// Check if thread exists and not running, then Start thread
		synchronized (PeerApp.class) {
			if (trackerConnectionThread == null || !trackerConnectionThread.isAlive()) {
				try {
					if (trackerConnectionThread == null) {
						trackerConnectionThread = new P2TConnectionThread(new Socket(trackerIP, trackerPort));
					}
					trackerConnectionThread.start();
				} catch (Exception e) {
					System.err.println("Failed to start tracker connection: " + e.getMessage());
				}
			} else {
				System.out.println("Tracker connection is already running");
			}
		}
	}

	public static void startListening() {
		// Check if thread exists and not running, then Start thread
		synchronized (PeerApp.class) {
			// Check if thread exists and not running
			if (peerListenerThread == null || !peerListenerThread.isAlive()) {
				try {
					if (peerListenerThread == null) {
						peerListenerThread = new P2PListenerThread(peerPort);
					}
					peerListenerThread.start();
				} catch (IOException e) {
					System.err.println("Failed to start peer listener: " + e.getMessage());
				}
			} else {
				System.out.println("Peer listener is already running");
			}
		}
	}

	public static void removeTorrentP2PThread(TorrentP2PThread torrentP2PThread) {
		if (torrentP2PThread == null) {
			return;
		}
		synchronized (torrentThreads) {
			torrentThreads.remove(torrentP2PThread);
			torrentP2PThread.end();
		}
	}

	public static void addTorrentP2PThread(TorrentP2PThread torrentP2PThread) {
		if (torrentP2PThread == null) {
			return;
		}
		synchronized (torrentThreads) {
			if (torrentThreads.contains(torrentP2PThread)) {
				return;
			}
			torrentThreads.add(torrentP2PThread);
			if (!torrentP2PThread.isAlive()) {
				torrentP2PThread.start();
			}
		}
	}

	public static String getSharedFolderPath() {
		return sharedFolderPath;
	}

	public static void addSentFile(String receiver, String fileNameAndHash) {
		synchronized (sentFiles) {
			sentFiles.computeIfAbsent(receiver, k -> new ArrayList<>());
			sentFiles.get(receiver).add(fileNameAndHash);
		}
	}

	public static void addReceivedFile(String sender, String fileNameAndHash) {
		synchronized (receivedFiles) {
			receivedFiles.computeIfAbsent(sender, k -> new ArrayList<>());
			receivedFiles.get(sender).add(fileNameAndHash);
		}
	}

	public static String getPeerIP() {
		return peerIP;
	}

	public static int getPeerPort() {
		return peerPort;
	}

	public static Map<String, List<String>> getSentFiles() {
		return sentFiles;
	}

	public static Map<String, List<String>> getReceivedFiles() {
		return receivedFiles;
	}

	public static P2TConnectionThread getP2TConnection() {
		return trackerConnectionThread;
	}

	public static void requestDownload(String ip, int port, String filename, String md5) throws Exception {
		// 1. Check if file already exists
		// 2. Create download request message
		// 3. Connect to peer
		// 4. Send request
		// 5. Receive file data
		// 6. Save file
		// 7. Verify file integrity
		// 8. Update received files list
		throw new UnsupportedOperationException("File download not implemented yet");
	}
}
