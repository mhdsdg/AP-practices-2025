package peer.app;

import common.utils.MD5Hash;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class TorrentP2PThread extends Thread {
	private final Socket socket;
	private final File file;
	private final String receiver;
	private final BufferedOutputStream dataOutputStream;

	public TorrentP2PThread(Socket socket, File file, String receiver) throws IOException {
		this.socket = socket;
		this.file = file;
		this.receiver = receiver;
		this.dataOutputStream = new BufferedOutputStream(socket.getOutputStream());
		PeerApp.addTorrentP2PThread(this);
	}

	@Override
	public void run() {
		try {
			// 1. Open file input stream
			java.io.FileInputStream fileInputStream = new java.io.FileInputStream(file);
			java.io.BufferedInputStream bufferedInputStream = new java.io.BufferedInputStream(fileInputStream);

			// 2. Read file in chunks and send to peer
			byte[] buffer = new byte[8192]; // 8KB buffer
			int bytesRead;
			long totalBytesSent = 0;

			while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
				dataOutputStream.write(buffer, 0, bytesRead);
				totalBytesSent += bytesRead;
			}

			// 3. Flush and close streams
			dataOutputStream.flush();
			end();

			// 4. Calculate MD5 hash of the file
			String md5Hash = MD5Hash.HashFile(file.getAbsolutePath());

			// 5. Update sent files list with file name and MD5 hash
			PeerApp.addSentFile(receiver, file.getName() + " " + md5Hash);

		} catch (IOException e) {
			System.err.println("File transfer error: " + e.getMessage());
		} finally {
			end();
		}
	}

	public void end() {
		try {
			dataOutputStream.close();
			socket.close();
		} catch (Exception e) {}
	}
}
