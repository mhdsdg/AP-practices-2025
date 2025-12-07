package peer.controllers;

import common.models.Message;
import common.utils.MD5Hash;
import peer.app.P2TConnectionThread;
import peer.app.PeerApp;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class P2TConnectionController {
	public static Message handleCommand(Message message) throws Exception {
		String command = message.getFromBody("command");
		Matcher matcher;
		if(PeerCommands.get_files_list.getMatcher(command).matches()){
			return getFilesList();
		}
		else if(PeerCommands.get_sends.getMatcher(command).matches()){
			return getSends();
		}
		else if(PeerCommands.get_receives.getMatcher(command).matches()){
			return getReceives();
		}
		else if(PeerCommands.status.getMatcher(command).matches()){
			return status();
		}
		else if((matcher = PeerCommands.file_request.getMatcher(command)).matches()){
			return sendFileRequest(PeerApp.getP2TConnection(), matcher.group("name"));
		}
		else{
			return new Message();
		}
	}

	private static Message getReceives() {
		HashMap<String, Object> map = new HashMap<>();
		Map<String, List<String>> receivedFiles = PeerApp.getReceivedFiles();
		map.put("received_files", receivedFiles);
		map.put("response", "ok");
		map.put("command", "get_receives");
		return new Message(map, Message.Type.response);
	}

	private static Message getSends() {
		HashMap<String, Object> map = new HashMap<>();
		Map<String, List<String>> sentFiles = PeerApp.getSentFiles();
		map.put("sent_files", sentFiles);
		map.put("response", "ok");
		map.put("command", "get_sends");
		return new Message(map, Message.Type.response);
	}

	public static Message getFilesList() {
		HashMap<String , Object> map = new HashMap<>();
		HashMap<String , String> filesMap = new HashMap<>();
		File folder = new File(PeerApp.getSharedFolderPath());
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				filesMap.put(file.getName(), MD5Hash.HashFile(file.getAbsolutePath()));
			}
		}
		map.put("command", "get_files_list");
		map.put("response", "ok");
		map.put("files", filesMap);
		return new Message(map, Message.Type.response);
	}

	public static Message status() {
		HashMap<String , Object> map = new HashMap<>();
		map.put("command", "status");
		map.put("response", "ok");
		map.put("peer", PeerApp.getPeerIP());
		map.put("listen_port", PeerApp.getPeerPort());
        return new Message(map, Message.Type.response);
	}

	public static Message sendFileRequest(P2TConnectionThread tracker, String fileName) throws Exception {
		// 1. Build request message
		try{
			HashMap<String, Object> fileRequest = new HashMap<>();
			fileRequest.put("name", fileName);
			// 2. Send message and wait for response
			Message result = tracker.sendAndWaitForResponse(new Message(fileRequest, Message.Type.file_request),PeerApp.TIMEOUT_MILLIS);
			return result;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		// 3. raise exception if error or return response
		return null;
	}
}
