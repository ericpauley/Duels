package zonedabone.Duels;

import java.util.HashMap;
import java.util.Map;

public class MessageParser {
	public static String parseMessage(String message, String src, String repl){
		Map<String,String> _repl = new HashMap<String,String>();
		_repl.put(src, repl);
		return parseMessage(message, _repl);
	}
	public static String parseMessage(String message, String src1, String repl1, String src2, String repl2){
		Map<String,String> _repl = new HashMap<String,String>();
		_repl.put(src1, repl1);
		_repl.put(src2, repl2);
		return parseMessage(message, _repl);
	}
	public static String parseMessage(String message){
		return parseMessage(message, new HashMap<String,String>());
	}
	public static String parseMessage(String message, Map<String,String> repl){
		message = Duels.MESSAGE_PREFIX+message;
		message = message.replace('&', '\u00A7');
		String[] torepl = repl.keySet().toArray(new String[0]);
		for(int i = 0;i<torepl.length;i++){
			message = message.replace(torepl[i], repl.get(torepl[i]));
		}
		return message;
	}
}
