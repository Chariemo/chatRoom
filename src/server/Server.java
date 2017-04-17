package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;

import dao.FriendDAO;
import dao.GroupDAO;
import dao.MessageCacheDAO;
import dao.UserDAO;
import idao.DAOFactory;
import model.User;
import model.Friend;
import model.Group;
import model.MessageCache;

public class Server {

	private static final int DEFAULT_PORT = 6789;
	private int numbersThread = 100;
	private int sizeOfBuffer = 1024;
	private ServerSocketChannel server;
	private int port;
	private HashMap<String, User> userOnline = new HashMap<>();
	private Selector selector;
	private ExecutorService poolThread = Executors.newFixedThreadPool(numbersThread);
	private UserDAO userDAO = (UserDAO) DAOFactory.createUserDAO();
	private FriendDAO friendDAO = (FriendDAO) DAOFactory.createFriendDAO();
	private MessageCacheDAO messageCacheDAO = (MessageCacheDAO) DAOFactory.createMessageCacheDAO();
	private GroupDAO groupDAO = (GroupDAO) DAOFactory.createGroupDAO();
	
	public Server(int p) throws IOException {
		
		this.port = p;
		server = ServerSocketChannel.open();
		selector = Selector.open();
		ServerSocket serverSocket = server.socket();
		SocketAddress address = new InetSocketAddress(port);
		serverSocket.bind(address);
		server.configureBlocking(false);
		server.register(selector, SelectionKey.OP_ACCEPT);
	}
	
	public Server() throws IOException {
		
		this(DEFAULT_PORT);
	}
	
	
	public void addUserOnline(String user_account, User user) throws IOException {
		
		userOnline.put(user_account, user);
	}
	
	public User delandGetUserOnline(SocketChannel socketChannel) {
		
		Iterator<Entry<String, User>> iterator = userOnline.entrySet().iterator();
		Entry<String, User> entry;
		User user = null;
		
		while (iterator.hasNext()) {
			
			entry = iterator.next();
			if (entry.getValue().getClient().equals(socketChannel)) {
				
				user = entry.getValue();
				iterator.remove();
			}
		}
		return user;
	}
	
	public void handleConnection() throws IOException {
		
		System.out.println("Server has started");
		while (true) {
			
			try {
				selector.select();
			} catch (IOException e) {
				break;
			}
			
			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> keyIterator = readyKeys.iterator();
			
			try {
				while (keyIterator.hasNext()) {
					
					SelectionKey key = (SelectionKey) keyIterator.next();
					keyIterator.remove();
					
					if (key.isAcceptable()) {
						
						SocketChannel client = server.accept();		
						System.out.println("Client " + client.getRemoteAddress()+ " has connected the server");
						client.configureBlocking(false);
						SelectionKey keyClient = client.register(selector, SelectionKey.OP_READ);
						Boolean finished = true;
						keyClient.attach(finished);
					}
					if (key.channel().isOpen() && key.isReadable()) {	
						
						Boolean finished = (Boolean) key.attachment();
						if (finished) {
							
							finished = false;
							key.attach(finished);
							poolThread.execute(new MessageHandler(key));
						}	
					}
				}
			} catch (IOException e) {
				System.err.println(e);
			}		
		}
	}
	
	
	class MessageHandler implements Runnable {

		private SocketChannel client;
		private SelectionKey key;
		
		public MessageHandler(SelectionKey key) {
			
			this.client = (SocketChannel) key.channel();
			this.key = key;
		}
		
		@Override
		public void run() {
			
			try {
				
				client.socket().sendUrgentData(0);
				packetHandle();	
				Boolean finished = (Boolean) key.attachment();
				finished = true;
				key.attach(finished);
			} catch (IOException e) {
				
				User user = delandGetUserOnline(client);
				friendsStatus(user, "logout");
				System.err.println("User " + user + " disconnected");
			} finally {		
				
				Thread.currentThread().interrupt();
			}
		}
		
		//tcp粘包处理
		private void packetHandle() throws IOException {
			
			ByteBuffer buffer = ByteBuffer.allocate(sizeOfBuffer);
			ByteBuffer remainPacket = ByteBuffer.allocate(sizeOfBuffer);
			ByteBuffer nextPacekt = null;
			boolean ispacketHead = true;
			boolean isEnd = true;
			while (true) {
				
				if (client.read(buffer) <= 0) {
					
					if (isEnd) {
						break;
					}
					else {
						continue;
					}
				}
				isEnd = false;
				buffer.flip();				
				int limit = buffer.limit();
				
				if (ispacketHead && buffer.remaining() >= 4) { //够一个字节
					
					int sizeOfData = buffer.getInt();
					if (sizeOfData <= buffer.remaining()) {
						
						//完整包直接转发
						buffer.limit(sizeOfData + 4);
						forwardData(buffer);
						//处理下一个包
						
						buffer.limit(limit);
						buffer.position(sizeOfData + 4);
						if (!buffer.hasRemaining()) {
							
							isEnd = true;
						}
						remainPacket.clear();
						remainPacket.put(buffer);
						remainPacket.limit(remainPacket.capacity());
						ByteBuffer temp = buffer;
						buffer = remainPacket;
						remainPacket = temp;
						continue;
					}
					else {// 半包处理
						
						ispacketHead = false;
						nextPacekt = ByteBuffer.allocate(sizeOfData);
						nextPacekt.put(buffer);
						nextPacekt.limit(sizeOfData);
						buffer.clear();
						continue;
					}
					
				}
				else if (ispacketHead && buffer.remaining() < 4) {
					
					continue;
				}
				else if (!ispacketHead) {
					
					if (buffer.remaining() < nextPacekt.remaining()) {
						
						nextPacekt.put(buffer);
						buffer.clear();
						nextPacekt.limit(nextPacekt.capacity());
						continue;
					}
					else {
						
						buffer.limit(nextPacekt.remaining());
						nextPacekt.put(buffer);
						nextPacekt.flip();
						//转发
						forwardData(nextPacekt);
						
						//处理剩余packet；
						buffer.limit(limit);
						if (!buffer.hasRemaining()) {
							
							isEnd = true;
						}
						remainPacket.clear();
						remainPacket.put(buffer);
						remainPacket.limit(remainPacket.capacity());
						ByteBuffer temp = buffer;
						buffer = remainPacket;
						remainPacket = temp;
						ispacketHead = true;
					}
				}
			}	
		}
		
		private void forwardData(ByteBuffer buffer) throws IOException {
			
			StringBuilder Message = new StringBuilder();	
			Message.append(Charset.forName("UTF-8").decode(buffer));
			int  index = Message.toString().indexOf(":");
			String strMessage = null;
			String[] arrayHead = null;
			String protocol = null;
			String account = null;
			MessageCache messageCache = null;
			
			if (index == -1) {
				
				arrayHead = Message.toString().split("-");
			}  
			else {
				
				strMessage = Message.substring(index + 1, Message.length());
				arrayHead = Message.substring(0, index).split("-");
			}
		
			if (arrayHead.length > 1) {
				protocol = arrayHead[1];
				account = arrayHead[0];
			}
						
			if ("signin".equals(protocol)) { //account-signoff-passwd
				
				signIn(arrayHead);
				return;
			}
			else if ("signup".equals(protocol)) { //" "-signup-passwd-...
				
				signUp(arrayHead);
				return;
			}
			
			User user = userOnline.get(account);
			if (user == null || !client.equals(user.getClient())) {
				poolThread.execute(new SendPacket(client, "tip:Wrong Account"));
				return;
			}

			if ("signoff".equals(protocol)) { //account-signoff
				
				signOff(arrayHead);
			}
			else if ("person".equals(protocol)) { //account-person-to_account:message
				
				if (arrayHead.length > 2) {
					
					messageCache = new MessageCache();
					messageCache.setFrom_account(account);
					messageCache.setTo_account(arrayHead[2]);
					messageCache.setContent(strMessage);
					messageCache.setMessage_type(1);
					singleForward(messageCache, "person");
				}
			}
			else if ("group".equals(protocol)) { //account-group-to_groupId:message
				
				groupForward(strMessage, arrayHead);
			}
			else if ("modifyuser".equals(protocol)) { //account-modifyuser-update-passwd...
				
				modifyUser(arrayHead);
			}
			else if ("modifyfriend".equals(protocol)) { //account-modifyfriend-add/update/..-friend_account-..
				
				modifyFriend(arrayHead);
			}
			else if ("modifygroup".equals(protocol)) { //account-modifygroup-add/update/..-groupId-gname
				
				modifyGroup(arrayHead);
			}
			else if ("searchperson".equals(protocol)) { //account-searchperson-search_account
				
				searchPerson(arrayHead);
			}
			
		}
		
		//登录
		private void signIn(String[] arrayHead) throws IOException {
			
			String strResponse = null;
			String user_account = null;
			String user_passwd = null;
			boolean verification = false;
			User self = null;
			if (arrayHead.length > 2) {
				
				user_account = arrayHead[0];
				user_passwd = arrayHead[2];
				self = userDAO.searchUserByCondition(user_account);
				
				if (self != null && userOnline.containsKey(self.getUser_account())) {
					
					strResponse = user_account + "-signin:Already Sign In";
				}
				else {
					
					System.out.println("SignIn Verify..." + user_account);
					verification = userDAO.verify(user_account, user_passwd);
					if (verification) {
						
						System.out.println("Pass\nUser " + self + " has signed In");
						strResponse = user_account + "-signin:succeed";
						self.setClient(client);
						addUserOnline(self.getUser_account(), self);
						
					}
					else {
						
						System.out.println("Failed");
						strResponse = user_account + "-signin:failed";
					}
				}
			} else {
				
				strResponse = "signin:Protocol Wrong";
			}
			
			poolThread.execute(new SendPacket(client, strResponse));
			//用户登录后处理
			if (verification) {
				
				friendsStatus(self, "login");
				handleMessageCache(self);
			}
		}
		
		
		//传送朋友列表及上线提醒
		private void friendsStatus(User meUser, String attach) {
			
			StringBuilder strResponse = new StringBuilder();
			strResponse.append("friends:");
			HashMap<User, String> friends = new HashMap<>();
			if (meUser != null) {
				friends = friendDAO.searchAllFriend(meUser.getUser_account());
			}
			else {
				return;
			}
			User friend = null;
			User friendOnline = null;
			SocketChannel friendClient = null;
			strResponse.append(meUser.getUser_account());
			strResponse.append("-");
			strResponse.append(meUser.getUser_name());
			strResponse.append("-");
			strResponse.append(meUser.getUser_passwd());
			strResponse.append("-");
			//tel = null
			if (meUser.getUser_tel() == null) {
				strResponse.append(" -");
			}
			else {
				strResponse.append(meUser.getUser_tel());
				strResponse.append("-");
			}
			//email = null
			if (meUser.getUser_email() == null) {
				strResponse.append(" -");
			}
			else {
				strResponse.append(meUser.getUser_email());
				strResponse.append("-");
			}
			//icon = null
			if (meUser.getUser_icon() == null) {
				strResponse.append(" -online");
			}
			else {
				strResponse.append(meUser.getUser_icon());
				strResponse.append("-online");
			}
			strResponse.append(":");
			
			if (!friends.isEmpty()) {
				
				Iterator<Entry<User, String>> friendIterator = friends.entrySet().iterator();
				while (friendIterator.hasNext()) {
					
					Entry<User, String> entry = friendIterator.next();
					friend = entry.getKey();
					strResponse.append(friend.getUser_account());
					strResponse.append("-");
					strResponse.append(friend.getUser_name());
					strResponse.append("-");
					if (entry.getValue() == null) {
						strResponse.append(" -");
					}
					else {
						strResponse.append(entry.getValue());
						strResponse.append("-");
					}
					//tel = null
					if (friend.getUser_tel() == null) {
						strResponse.append(" -");
					}
					else {
						strResponse.append(friend.getUser_tel());
						strResponse.append("-");
					}
					//email = null
					if (friend.getUser_email() == null) {
						strResponse.append(" -");
					}
					else {
						strResponse.append(friend.getUser_email());
						strResponse.append("-");
					}
					//icon = null
					if (friend.getUser_icon() == null) {
						strResponse.append(" -");
					}
					else {
						strResponse.append(friend.getUser_icon());
						strResponse.append("-");
					}
					if ((friendOnline = userOnline.get(friend.getUser_account())) != null) {
						strResponse.append("online"); //在线
						
						friendClient = friendOnline.getClient();
						poolThread.execute(new SendPacket(friendClient, attach + "-" + meUser.getUser_account()));
					}
					else {
						strResponse.append("notonline"); //不在线
					}
					strResponse.append(":");
				}
				if ("login".equals(attach)) {
					poolThread.execute(new SendPacket(client, strResponse.toString()));
				}
			}
		}
		
		//获取并转发离线信息
		private void handleMessageCache(User user) throws IOException {
			
			List<MessageCache> messageList = messageCacheDAO.searchMessageCache(user.getUser_account());
			StringBuilder data = new StringBuilder();
			data.append("person-");
			if (!messageList.isEmpty()) {
				
				for (MessageCache messageCache : messageList) {
					
					if (messageCache.getMessage_type() == 1) {
						singleForward(messageCache, "person");
					}
					else if (messageCache.getMessage_type() == -1) {
						
					}
					else {
						singleForward(messageCache, "verification");
					}
				}
			}
			
			//TODO 进群验证 好友验证
			
		}
		
		//注册
		private void signUp(String[] arrayHead) throws IOException {
			
			System.out.println("SignUp...");
			User newUser = new User();
			if (arrayHead.length > 6) {
				newUser.setUser_name(arrayHead[2]);
				newUser.setUser_passwd(arrayHead[3]);
				newUser.setUser_tel(arrayHead[4]);
				newUser.setUser_email(arrayHead[5]);
				newUser.setUser_icon(arrayHead[6]);
			}
			else {
				
				poolThread.execute(new SendPacket(client, "signup:Protocol Wrong"));
				return;
			}
			
			String strResponse = null;
			String random_account = null;
			if (null != (random_account = userDAO.insert(newUser))) {
				
				System.out.println("Success");
				strResponse = random_account + "-signup:succeed";
			}
			else {
				
				System.out.println("Failed");
				strResponse = "signup:failed";
			}
			
			poolThread.execute(new SendPacket(client, strResponse));
		}
		
		//注销
		private void signOff(String[] arrrayHead) throws IOException {
			
			String strResponse = "signoff:";
			
			if (userDAO.delete(arrrayHead[0]))	{
				
				delandGetUserOnline(client);
				strResponse += "succeed";
				poolThread.execute(new SendPacket(client, strResponse));
				key.cancel();
				client.close();
			}
			else {
				
				strResponse += "failed";
				poolThread.execute(new SendPacket(client, strResponse));
			}
		}
		
		//单发
		private void singleForward(MessageCache messageCache, String attach) throws IOException {
			
			StringBuilder messageResponse = new StringBuilder();
			User toUser = userDAO.searchUserByCondition(messageCache.getTo_account());
			SocketChannel clientTarget = null;
			
			if (toUser == null) {
				
				poolThread.execute(new SendPacket(clientTarget, "person:There is Who"));
				return;
			}
			
			if (!userOnline.containsKey(toUser.getUser_account())) {
				
				messageCacheDAO.insert(messageCache);
				return;
				
			}
			
			clientTarget = userOnline.get(toUser.getUser_account()).getClient();
			messageResponse.append(attach);
			messageResponse.append("-");
			messageResponse.append(messageCache.getFrom_account());
			messageResponse.append(":");
			messageResponse.append(messageCache.getContent());
			
			if (messageCache.getMessage_type() != 1) {
				messageResponse.append(":");
				messageResponse.append(messageCache.getMessage_type());
			}
			poolThread.execute(new SendPacket(clientTarget, messageResponse.toString()));
		}
		
		//组发
		private void groupForward(String strMessage, String[] arrayHead) throws IOException {
			
			String from_account = null;
			int group_id  = 0;
			MessageCache messageCache = null;
			if (arrayHead.length > 2) {
				
				from_account = arrayHead[0];
				group_id = Integer.parseInt(arrayHead[2]);
			}
			else {
				poolThread.execute(new SendPacket(client, "group:Protocol Wrong"));
				return;
			}
			
			
			List<User> groupUsers = groupDAO.searchAllUsersByGroup(group_id, from_account);
			if (groupUsers == null || groupUsers.isEmpty()) {
				poolThread.execute(new SendPacket(client, "group:Wrong Account Or GroupId"));
				return;
			}
			for (User user : groupUsers) {
				
				messageCache = new MessageCache();
				messageCache.setFrom_account(from_account);
				messageCache.setTo_account(user.getUser_account());
				messageCache.setContent(strMessage);
				messageCache.setMessage_type(1);
				singleForward(messageCache, "group");
			}			
		}
		
		private void modifyUser(String[] arrayHead) throws IOException {
			
			if (arrayHead.length > 7) {
				if ("update".equals(arrayHead[2])) {
					
					User user = new User();
					user.setUser_account(arrayHead[0]);
					user.setUser_name(arrayHead[3]);
					user.setUser_passwd(arrayHead[4]);
					user.setUser_tel(arrayHead[5]);
					user.setUser_email(arrayHead[6]);
					user.setUser_icon(arrayHead[7]);
					if (userDAO.update(user)) {
						
						poolThread.execute(new SendPacket(client, "modifyuser-update:succeed"));
						return;
					}
					else {
						
						poolThread.execute(new SendPacket(client, "modifyuser-update:failed"));
						return;
					}
				}
			}
			poolThread.execute(new SendPacket(client, "modifyuser-update:Protocol Wrong"));
		}
		
		private void modifyFriend(String[] arrayHead) throws IOException {
			
			String strResponse = "modifyfriend";
			String method = arrayHead[2];
			MessageCache messageCache = null;
			Friend friend = new Friend(); 
			
			if ("deleteall".equals(method)){
				
				if (friendDAO.deleteAllFriends(arrayHead[0])) {
					strResponse += "-Delete all friend succeed";
				}
				else {
					strResponse += "-Delete all friend failed";
				}
				poolThread.execute(new SendPacket(client, strResponse));
				return;
			}
			if (arrayHead.length > 4) {
				
				friend.setUser_account(arrayHead[0]);
				friend.setFriend_account(arrayHead[3]);
				friend.setFriend_remark(arrayHead[4]);
			} else {
				
				poolThread.execute(new SendPacket(client, "modifyfriend:Protocol Wrong"));
				return;
			}
			
			if ("add".equals(method)) {
				
				String status = arrayHead[5];
				System.out.println("modifyfriend:fff " + status);
				if ("ask".equals(status) && !friend.getUser_account().equals(friend.getFriend_account())) {
					messageCache = new MessageCache();
					messageCache.setFrom_account(friend.getUser_account());
					messageCache.setTo_account(friend.getFriend_account());
					messageCache.setContent(userOnline.get(friend.getUser_account()) + " want to add you as a friend");
					messageCache.setMessage_type(0);
					singleForward(messageCache, "verification");
					return;
				}
				else if ("succeed".equals(status)) {
					if (friendDAO.addFriend(friend)) {
						return;
					}
					else {
						strResponse += "-add:failed";
					}
				}
				else if ("failed".equals(status)) {
					
					
					messageCache = new MessageCache();
					messageCache.setFrom_account(friend.getUser_account());
					messageCache.setTo_account(friend.getFriend_account());
					messageCache.setContent(userOnline.get(friend.getUser_account()) + " don't not agree"
							+ " add you as a friend");
					messageCache.setMessage_type(22);
					singleForward(messageCache, "verification");
					return;
				}
				
				/*if (friendDAO.addFriend(friend)) {
					strResponse += ("-Add friend " + friend.getFriend_account() + " succeed");	
				}
				else {
					strResponse += ("-Add friend " + friend.getFriend_account() + " failed");
				}*/
			}
			else if ("update".equals(method)) {
				
				if (friendDAO.updateFriend(friend)) {
					strResponse += ("-update " + friend.getFriend_remark() + " " + friend.getFriend_account() + " succeed");
				}
				else {
					strResponse += ("-update " + friend.getFriend_remark() + " " + friend.getFriend_account() + " failed");
				}
			}
			else if ("delete".equals(method)){
				
				if (friendDAO.deleteFriendByAccount(friend.getUser_account(), friend.getFriend_account())) {
					strResponse += ("-delete friend " + friend.getFriend_account() + " succeed");
				}
				else {
					strResponse += ("-delete friend " + friend.getFriend_account() + " failed");
				}
			}
			poolThread.execute(new SendPacket(client, strResponse));
		}
		
		private void modifyGroup(String[] arrayHead) throws IOException {
			
			String method = null;
			String strResponse = "modifygroup";
			Group group = new Group();
			if (arrayHead.length > 4) {
				
				method = arrayHead[2];
				group.setUser_account(arrayHead[0]);
				group.setGroup_id(Integer.parseInt(arrayHead[3]));
				group.setGroup_name(arrayHead[4]);
			}
			else {
				
				poolThread.execute(new SendPacket(client, "modifygroup: Wrong Protocol"));
				return;
			}
			
			if ("add".equals(method)) {
				
				if (groupDAO.createGroup(group)) {
					strResponse += "-add:succeed";
				}
				else {
					strResponse += "-add:failed";
				}
			}
			else if ("update".equals(method)) {
				
				if (groupDAO.updateGroup(group)) {
					strResponse += "-update:succeed";
				}
				else {
					strResponse += "-update:failed";
				}
			}
			else {
				
				if (groupDAO.deleteGroup(group.getGroup_id(), group.getUser_account())) {
					strResponse += "-delete:succeed";
				}
				else {
					strResponse += "-delete:failed";
				}
			}
			poolThread.execute(new SendPacket(client, strResponse));
		}
		
		private void searchPerson(String[] arrayHead) {
			
			User searchUser = null;
			StringBuilder strResponse = new StringBuilder();
			strResponse.append("searchperson:");
			if (arrayHead.length > 2) {
				
				searchUser = userDAO.searchUserByCondition(arrayHead[2]);
				if (searchUser == null) {
					strResponse.append("There is no this guy");
				}
				else {
					strResponse.append(searchUser.getUser_account());
					strResponse.append("-");
					strResponse.append(searchUser.getUser_name());
					strResponse.append("-");
					strResponse.append(searchUser.getUser_email());
				}
				poolThread.execute(new SendPacket(client, strResponse.toString()));
			}
		}
		
	}
	
	
	class SendPacket implements Runnable {
		
		private SocketChannel client;
		private String strResponse;
		
		public SendPacket(SocketChannel client, String strResponse) {
			this.client = client;
			this.strResponse = strResponse;
		}
		
		@Override
		public void run() {
			
			byte[] byteResponse;
			try {
				System.out.println(strResponse);
				byteResponse = strResponse.getBytes("UTF-8");
				int lengthResponse = byteResponse.length;
				ByteBuffer buffer = ByteBuffer.allocate(lengthResponse + 4);
				buffer.putInt(lengthResponse);
				buffer.put(byteResponse);
				buffer.flip();
				while (buffer.hasRemaining()) {
					
					client.write(buffer);
				}
			} catch (IOException e) {
				System.err.println(e);
			}
		}		
	}
	
	public static void main(String[] args){
	
		Server server;
		try {
			server = new Server();
			server.handleConnection();
		} catch (IOException e) {
			System.err.println(e);
		}
				
		
	}
}




