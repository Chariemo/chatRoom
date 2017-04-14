package server;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import dao.MessageCacheDAO;
import dao.UserDAO;
import idao.DAOFactory;
import idao.IMessageCacheDAO;
import model.MessageCache;
import model.User;

public class TestDAO {
	
	public static void main(String[] args) {
		
		/*User user = new User();
		user.setUser_account("654321");
		user.setUser_icon("/src");
		user.setUser_name("abc");
		user.setUser_passwd("111111");
		
		UserDAO userDAO = (UserDAO) DAOFactory.createUserDAO();
		userDAO.insert(user);
		System.out.println(userDAO.verify("123456", "111111"));
		List<User> users = userDAO.searchUserByName("charley");
		System.out.println(users.get(0));*/
		
		 
		/*Friend friend = new Friend();
		friend.setFriend_account("123465");
		friend.setUser_account("654321");
		IFriendDAO friendDAO = DAOFactory.createFriendDAO();
		
		friendDAO.addFriend(friend);
		System.out.println(friendDAO.searchFriendsByRemark("654321", "charley").get(0));
		*/
		
		MessageCacheDAO messageCacheDAO = (MessageCacheDAO) DAOFactory.createMessageCacheDAO();
		MessageCache messageCache = new MessageCache();

//		messageCacheDAO.insert("123456", "654321", "nishishabi");
		System.out.println(messageCacheDAO.searchMessageCache("654321").get(0).toString());
	}
}
