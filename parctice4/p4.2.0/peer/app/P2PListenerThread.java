package peer.app;

import common.models.Message;
import common.utils.JSONUtils;
import common.utils.MD5Hash;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static peer.app.PeerApp.TIMEOUT_MILLIS;

public class P2PListenerThread extends Thread {
	private final ServerSocket serverSocket;

	public P2PListenerThread(int port) throws IOException {
		this.serverSocket = new ServerSocket(port);
	}

	private void handleConnection(Socket socket) throws Exception {
		boolean download = false;
		try {
			socket.setSoTimeout(TIMEOUT_MILLIS);

			DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
			String message = dataInputStream.readUTF();

			if (message.isEmpty()) {
				socket.close();
				return;
			}

			Message messageObj = JSONUtils.fromJson(message);
			if(messageObj.getType().equals(Message.Type.download_request)){
				String fileName = (String) messageObj.getFromBody("name");
				String expectedHash = (String) messageObj.getFromBody("md5");
				File sendFile = new File(PeerApp.getSharedFolderPath(), fileName);

				// Verify file exists and hash matches before sending
				if(!sendFile.exists() || !MD5Hash.HashFile(sendFile.getAbsolutePath()).equals(expectedHash)) {
					socket.close();
					return;
				}

				// Start transfer thread
				TorrentP2PThread transferThread = new TorrentP2PThread(
						socket,
						sendFile,
						messageObj.getFromBody("receiver_ip") + ":" + messageObj.getIntFromBody("receiver_port")
				);
				download = true;
			}
		} catch (Exception e) {
			System.err.println("Error handling connection: " + e.getMessage());
		}
		finally {
			if(!download) {
				socket.close();
			}
		}
	}

	@Override
	public void run() {
		while (!PeerApp.isEnded()) {
			try {
				Socket socket = serverSocket.accept();
				handleConnection(socket);
			} catch (Exception e) {
				break;
			}
		}

		try {serverSocket.close();} catch (Exception ignored) {}
	}
}
