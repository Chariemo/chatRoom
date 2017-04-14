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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
	
	public void delUserOnline(SocketChannel socketChannel) {
		
		Set<String> keys = userOnline.keySet();
		for (String key : keys) {
			
			if (userOnline.get(key).getClient().equals(socketChannel)) {
				userOnline.remove(key);
			}
		}
	}
	
	public void handleConnection() {
		
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
						System.out.println("User " + client.getRemoteAddress()+ " has connected the server");
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
							poolThread.execute(new MessageHandle(key));
						}	
					}
				}
			} catch (IOException e) {
				System.err.println(e);
			}		
		}
	}
	
	
	class MessageHandle implements Runnable {

		private SocketChannel client;
		private SelectionKey key;
		
		public MessageHandle(SelectionKey key) {
			
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
				
				delUserOnline(client);
				try {
					
					System.err.println("User " + client.getRemoteAddress() + " disconnected");
				} catch (IOException e1) {
					
				}
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
			
			if (index == -1) {
				
				arrayHead = Message.toString().split("-");
			}
			else {
				
				strMessage = Message.substring(index + 1, Message.length());
				arrayHead = Message.substring(0, index).split("-");
			}
			
			if ("signin".equals(arrayHead[0])) {
				
				signIn(arrayHead);
			}
			else if ("signup".equals(arrayHead[0])) {
				
				signUp(arrayHead);
			}
			else if ("signoff".equals(arrayHead[0])) {
				
				signOff(arrayHead);
			}
			else if ("person".equals(arrayHead[0])) {
				
				singleForward(arrayHead[1], arrayHead[2], strMessage, "person");
			}
			else if ("group".equals(arrayHead[0])) {
				
				groupForward(strMessage, arrayHead);
			}
			else if ("modifyuser".equals(arrayHead[0])) {
				
				modifyUser(arrayHead);
			}
			else if ("modifyfriend".equals(arrayHead[0])) {
				
				modifyFriend(arrayHead);
			}
			else if ("modifygroup".equals(arrayHead[0])) {
				
				modifyGroup(arrayHead);
			}
			
		}
		
		//登录
		private void signIn(String[] arrayHead) throws IOException {
			
			String strResponse = "signin:";
			String user_account = null;
			String user_passwd = null;
			boolean verification = false;
			User user = null;
			if (arrayHead.length > 2) {
				
				user_account = arrayHead[1];
				user_passwd = arrayHead[2];
				user = userDAO.searchUserByCondition(user_account);
				
				if (user != null && userOnline.containsKey(user.getUser_account())) {
					
					strResponse += "Already Sign In!";
				}
				else {
					
					System.out.println("SignIn Verify..." + user_account);
					verification = userDAO.verify(user_account, user_passwd);
					if (verification) {
						
						System.out.println("Pass");
						strResponse += "succeed";
						user.setClient(client);
						addUserOnline(user.getUser_account(), user);
						
					}
					else {
						
						System.out.println("Failed");
						strResponse += "failed";
					}
				}
			} else {
				
				strResponse += "Protocol Wrong";
			}
			
			sendPacket(client, strResponse);
			//获取并转发离线信息
			if (verification) {
				handleMessageCache(user);
			}
		}
		
		//获取并转发离线信息
		private void handleMessageCache(User user) throws IOException {
			
			List<MessageCache> messageList = messageCacheDAO.searchMessageCache(user.getUser_account());
			StringBuilder data = new StringBuilder("person-");
			if (!messageList.isEmpty()) {
				
				for (MessageCache messageCache : messageList) {
					
					data.append(messageCache.getFrom_account());
					data.append(":");
					data.append(messageCache.getContent());
					sendPacket(client, data.toString());
				}
			}
			
		}
		
		//注册
		private void signUp(String[] arrayHead) throws IOException {
			
			System.out.println("SignUp...");
			User newUser = new User();
			System.out.println("length: " + arrayHead.length);
			if (arrayHead.length > 5) {
				newUser.setUser_name(arrayHead[1]);
				System.out.println(newUser.getUser_name());
				newUser.setUser_passwd(arrayHead[2]);
				newUser.setUser_tel(arrayHead[3]);
				newUser.setUser_email(arrayHead[4]);
				newUser.setUser_icon(arrayHead[5]);
			}
			else {
				
				sendPacket(client, "signup:Protocol Wrong");
				return;
			}
			
			String strResponse = null;
			String random_account = null;
			if (null != (random_account = userDAO.insert(newUser))) {
				
				System.out.println("Success");
				
				strResponse = "signup:" + random_account;
			}
			else {
				
				System.out.println("Failed");
				strResponse = "signup:failed";
			}
			
			sendPacket(client, strResponse);
		}
		
		//注销
		private void signOff(String[] arrrayHead) throws IOException {
			
			String strResponse = "signoff:";
			User user = null;
			if (arrrayHead.length > 1) {
				
				user = userOnline.get(arrrayHead[1]);
			}
			else {
				
				sendPacket(client, "signoff:Protocol Wrong");
				return;
			}
			
			if (user == null || !user.getClient().equals(client)) {
				
				strResponse += "Wrong Account";
				sendPacket(client, strResponse);
			}
			else {
				
				if (userDAO.delete(user.getUser_account()))	{
					
					delUserOnline(client);
					strResponse += "succeed";
					sendPacket(client, strResponse);
					client.close();
				}
				else {
					
					strResponse += "failed";
					sendPacket(client, strResponse);
				}
			}
			System.out.println(strResponse);
		}
		
		//单发
		private void singleForward(String from_account, String to_account, String strMessage, String attach) throws IOException {
			
			StringBuilder messageResponse = new StringBuilder();
			User target = userDAO.searchUserByCondition(to_account);
			SocketChannel clientTarget = null;
			User from_user = userOnline.get(from_account);
			
			if (from_account == null || !client.equals(from_user.getClient())) {
				
				sendPacket(clientTarget, "person:Wrong Account");
				return;
			}
			if (target == null) {
				
				sendPacket(clientTarget, "person:There is Who?");
				return;
			}
			
			if (!userOnline.containsKey(target.getUser_account())) {
				
				messageCacheDAO.insert(from_account, to_account, strMessage);
				return;
				
			}
			
			clientTarget = userOnline.get(target.getUser_account()).getClient();
			messageResponse.append(attach);
			messageResponse.append("-");
			messageResponse.append(from_account);
			messageResponse.append(":");
			messageResponse.append(strMessage);
			System.out.println(messageResponse.toString());
			sendPacket(clientTarget, messageResponse.toString());
		}
		
		//组发
		private void groupForward(String strMessage, String[] arrayHead) throws IOException {
			
			String from_account = null;
			int group_id  = 0;
			if (arrayHead.length > 2) {
				
				from_account = arrayHead[1];
				group_id = Integer.parseInt(arrayHead[2]);
			}
			else {
				sendPacket(client, "groupErr:Protocol Wrong");
				return;
			}
			
			
			List<User> groupUsers = groupDAO.searchAllUsersByGroup(group_id, from_account);
			if (groupUsers == null || groupUsers.isEmpty()) {
				sendPacket(client, "groupErr:Wrong Account Or GroupId");
				return;
			}
			for (User user : groupUsers) {
				
				singleForward(from_account, user.getUser_account(), strMessage, "group");
			}			
		}
		
		private void sendPacket(SocketChannel client, String strResponse) throws IOException {
			
			byte[] byteResponse = strResponse.getBytes("UTF-8");
			int lengthResponse = byteResponse.length;
			ByteBuffer buffer = ByteBuffer.allocate(lengthResponse + 4);
			buffer.putInt(lengthResponse);
			buffer.put(byteResponse);
			buffer.flip();
			while (buffer.hasRemaining()) {
				
				client.write(buffer);
			}
		}
		
		private void modifyUser(String[] arrayHead) throws IOException {
			
			String method = arrayHead[1];
			String user_account = arrayHead[2];
			
			if (arrayHead.length > 7) {
				
				User user = userOnline.get(user_account);
				if (user != null && user.getClient().equals(client)) {
					
					if ("update".equals(method)) {
						
						user.setUser_account(user_account);
						user.setUser_name(arrayHead[3]);
						user.setUser_passwd(arrayHead[4]);
						user.setUser_tel(arrayHead[5]);
						user.setUser_email(arrayHead[6]);
						user.setUser_icon(arrayHead[7]);
						if (userDAO.update(user)) {
							
							sendPacket(client, "modifyUser-update:succeed");
							return;
						}
						else {
							
							sendPacket(client, "modifyUser-update:failed");
							return;
						}
					}	
				}
			}
			sendPacket(client, "modifyUser-update:Protocol Wrong");
		}
		
		private void modifyFriend(String[] arrayHead) throws IOException {
			
			String strResponse = "modifyfriend";
			String method = null;
			Friend friend = new Friend(); 
			if (arrayHead.length > 4) {
				
  				method = arrayHead[1];
				friend.setUser_account(arrayHead[2]);
				friend.setFriend_account(arrayHead[3]);
				friend.setFriend_remark(arrayHead[4]);
			} else {
				sendPacket(client, "modifyfriend:Protocol Wrong");
				return;
			}
			
			User user = userOnline.get(friend.getUser_account());
			
			if (user != null && user.getClient().equals(client)) {
				
				if ("add".equals(method)) {
					
					if (friendDAO.addFriend(friend)) {
						strResponse += ("-Add friend " + friend.getFriend_account() + " succeed");	
					}
					else {
						strResponse += ("-Add friend " + friend.getFriend_account() + " failed");
					}
				}
				else if ("update".equals(method)) {
					
					if (friendDAO.updateFriend(friend)) {
						strResponse += ("-Update friend " + friend.getFriend_account() + " succeed");
					}
					else {
						strResponse += ("-Update friend " + friend.getFriend_account() + " failed");
					}
				}
				else if ("delete".equals(method)){
					
					if (friendDAO.deleteFriendByAccount(friend.getUser_account(), friend.getFriend_account())) {
						strResponse += ("-Delete friend " + friend.getFriend_account() + " succeed");
					}
					else {
						strResponse += ("-Delete friend " + friend.getFriend_account() + " failed");
					}
				}
				else if ("deleteall".equals(method)){
					
					if (friendDAO.deleteAllFriends(user.getUser_account())) {
						strResponse += ("-Delete all friend " + friend.getFriend_account() + " succeed");
					}
					else {
						strResponse += ("-Delete all friend " + friend.getFriend_account() + " failed");
					}
				}
			}
			else {
				strResponse += ":Wrong Account";
			}
			System.out.println(strResponse);
			sendPacket(client, strResponse);
		}
		
		private void modifyGroup(String[] arrayHead) throws IOException {
			
			String method = null;
			String strResponse = "modifygroup";
			Group group = new Group();
			if (arrayHead.length > 4) {
				
				method = arrayHead[1];
				group.setUser_account(arrayHead[2]);
				group.setGroup_id(Integer.parseInt(arrayHead[3]));
				group.setGroup_name(arrayHead[4]);
			}
			else {
				
				sendPacket(client, "modifygroup: Wrong Protocol");
				return;
			}
			
			User user = userOnline.get(group.getUser_account());
			if (user == null || !client.equals(user.getClient())) {
				
				sendPacket(client, "modifygroup: Wrong Account");
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
			sendPacket(client, strResponse);
		}
		
	}
	
	
	class Writer implements Runnable {
		
		private SocketChannel client;
		
		public Writer(SocketChannel client) {
			this.client = client;
			
		}
		
		@Override
		public void run() {
			
			
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




