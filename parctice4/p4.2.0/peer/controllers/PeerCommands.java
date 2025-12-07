package peer.controllers;

import common.models.CLICommands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum PeerCommands implements CLICommands {
	END("exit"),
	TODO("TODO"),
	status("status"),
	get_files_list("get_files_list"),
	get_sends("get_sends"),
	get_receives("get_receives"),
	file_request("file_request\\s+(?<name>.+?)"),
	List("list"),
	Download("download\\s+(?<name>.+?)"),
	;


	private final String regex;

	PeerCommands(String regex) {
		this.regex = regex;
	}

	@Override
	public Matcher getMatcher(String input) {
		return Pattern.compile(regex).matcher(input);
	}
}
