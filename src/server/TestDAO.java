package server;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import com.sun.org.apache.bcel.internal.generic.NEW;

import dao.KeyIncrementer;
import dao.MessageCacheDAO;
import dao.UserDAO;
import idao.DAOFactory;
import idao.IMessageCacheDAO;
import model.MessageCache;
import model.User;

public class TestDAO {
	
	public static void main(String[] args) {
		
		User user = new User();
		user.setUser_name("abc");
		user.setUser_passwd("123456");
		
		UserDAO userDAO = (UserDAO) DAOFactory.createUserDAO();
		userDAO.insert(user);
		System.out.println(userDAO.verify("000001", "123456"));
		List<User> users = userDAO.searchUserByName("abc");
		System.out.println(users.get(0));
		
		 
		/*Friend friend = new Friend();
		friend.setFriend_account("123465");
		friend.setUser_account("654321");
		IFriendDAO friendDAO = DAOFactory.createFriendDAO();
		
		friendDAO.addFriend(friend);
		System.out.println(friendDAO.searchFriendsByRemark("654321", "charley").get(0));
		*/
//		
//		MessageCacheDAO messageCacheDAO = (MessageCacheDAO) DAOFactory.createMessageCacheDAO();
//		MessageCache messageCache = new MessageCache();
//
//		messageCacheDAO.insert("123456", "654321", "nishishabi");
//		System.out.println(messageCacheDAO.searchMessageCache("654321").get(0).toString());
		
//		String start = "000001";
//		System.out.println(Integer.toString((Integer.parseInt(start) + 1)));
		
//		System.out.println(new KeyIncrementer().nextKey("300005"));
	}
}
