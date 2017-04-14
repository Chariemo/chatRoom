package idao;

import java.util.List;

import model.MessageCache;

public interface IMessageCacheDAO {
	
	public boolean insert(String from_account, String to_account, String content);
	
	public List<MessageCache> searchMessageCache(String to_account);
	
}
