package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;

import com.sun.javafx.applet.Splash;

import model.MessageCache;
import model.User;

public class Client {
	private Selector selector;
	private InetSocketAddress serverAddress;
	private SocketChannel socketChannel;

	private final int BLOCK = 2048;
	private final ByteBuffer sendBuffer = ByteBuffer.allocate(BLOCK);
	private final ByteBuffer receiveBuffer = ByteBuffer.allocate(BLOCK);

	private Charset charset = Charset.forName("UTF-8");

	private static boolean isSignIn;

	private SignInView signInView;
	private FriendsView friendsView;
	private String selfAccount;

	public Client(InetSocketAddress socketAddress) {
		this.serverAddress = socketAddress;
	}

	private void socketChannelInfo() throws IOException {
		selector = Selector.open();
		socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
		socketChannel.register(selector, SelectionKey.OP_CONNECT);
		socketChannel.connect(serverAddress);

		try {
			infoSignInView();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	private void infoSignInView() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException {

		for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
			if ("Nimbus".equals(info.getName())) {
				javax.swing.UIManager.setLookAndFeel(info.getClassName());
				break;
			}
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				signInView = new SignInView(socketChannel, sendBuffer);
				signInView.setVisible(true);
				signInView.setLocationRelativeTo(null);
			}
		});
	}

	public void start() throws IOException {

		socketChannelInfo();
		Thread read = new Thread(new read());
		read.start();

		// while (true){
		//
		// }
		// Scanner scanner = new Scanner(System.in);
		// String line = "";
		// while(!read.isInterrupted() && (line = scanner.nextLine()) != null){
		// sendBuffer.putInt(line.getBytes().length);
		// sendBuffer.put(charset.encode(line).array(), 0,
		// line.getBytes().length);
		// sendBuffer.flip();
		// while(sendBuffer.hasRemaining()) {
		// socketChannel.write(sendBuffer);
		// }
		// sendBuffer.clear();
		// }
	}

	private class read implements Runnable {

		@Override
		public void run() {
			try {
				while (true) {
					selector.select();
					for (SelectionKey selectionKey : selector.selectedKeys()) {
						selector.selectedKeys().remove(selectionKey);
						if (selectionKey.isConnectable()) {
							System.out.println("client connect");
							socketChannel = (SocketChannel) selectionKey.channel();
							if (socketChannel.isConnectionPending()) {
								socketChannel.finishConnect();
								System.out.println("achieve connectionPending");
							}

							selectionKey.interestOps(SelectionKey.OP_READ);
						} else if (selectionKey.isReadable()) {

							SocketChannel keyChannel = (SocketChannel) selectionKey.channel();

							keyChannel.socket().sendUrgentData(0);

							receiveBuffer.clear();
							if (isSignIn) {
								handleMessage(readMessage(keyChannel));
							} else {
								handleSignMessage(readMessage(keyChannel));
							}

							selectionKey.interestOps(SelectionKey.OP_READ);
						}
					}
				}
			} catch (IOException e) {
				Thread.currentThread().interrupt();
				System.err.println("The server is error ");
			}
		}
	}

	private String readMessage(SocketChannel keyChannel) {
		String line = "";
		try {
			int rlen = 0;
			receiveBuffer.position(0);
			receiveBuffer.limit(4);
			int tmp = 0;
			while (true) {
				rlen = keyChannel.read(receiveBuffer);
				receiveBuffer.flip();
				if (rlen != 0 && rlen != -1)
					tmp += rlen;
				if (tmp == 4)
					break;
			}
			int length = receiveBuffer.getInt();
			receiveBuffer.limit(length);
			receiveBuffer.position(0);
			rlen = 0;
			tmp = 0;
			while (true) {
				rlen = keyChannel.read(receiveBuffer);
				if (rlen != 0 && rlen != -1) {
					tmp += rlen;
					receiveBuffer.flip();
					line += charset.decode(receiveBuffer);
					receiveBuffer.clear();
				}
				if (tmp == length)
					break;
			}
		} catch (IOException e) {
			System.err.println(e);
		}
		System.out.println("line: " + line);
		return line;
	}

	private void handleSignMessage(String message) {
		if (!message.equals("signin:Protocol Wrong") || !message.equals("signup:Protocol Wrong")) {
			String protocol;
			String account;
			String content;
			if (message.split(":")[0].split("-").length > 1){
				protocol = message.split(":")[0].split("-")[1];
				content = message.split(":")[1];
			} else {
				protocol = message.split(":")[0];
				content = message.split(":")[1];
			}
			account = message.split(":")[0].split("-")[0];

			if (("signin").equals(protocol)) {
				if (("succeed").equals(content)) {
					isSignIn = true;
					System.out.println("Already connect to server");
					signInView.dispose();
					
					selfAccount = account;
					friendsView = new FriendsView(account, socketChannel);
					friendsView.getJFrame().setVisible(true);
					friendsView.getJFrame().setLocation(1100, 60);
					
				} else if (("Already Sign In").equals(content)) {
					isSignIn = false;
					JOptionPane.showMessageDialog(null, "该用户已经登陆！");
				} else {
					isSignIn = false;
					JOptionPane.showMessageDialog(null, "账号或密码错误！");
				}
			}
			if (("signup").equals(protocol))
				if (content.equals("Failed")) {
					JOptionPane.showMessageDialog(null, "注册失败！");
				} else if (content.equals("succeed")) {
					JOptionPane.showMessageDialog(null, "您的账号是： " + account);
					signInView.disposeSignUpView();
				} else {
					JOptionPane.showMessageDialog(null, "请填写完整的信息！");
				}
		} else {
			JOptionPane.showMessageDialog(null, "网络传输错误！");
		}
	}

	private void handleMessage(String message) {
		
		int index = message.indexOf(":");
		String content = null;
		String tmp = null;
		String protocol = null;
		
		if (index != -1){
			
			content = message.substring(index + 1, message.length());
			tmp = message.substring(0, index);
			protocol = tmp.split("-")[0];
		} else {
			
			index = message.indexOf("-");
			protocol = message.substring(0, index);
			tmp = message.substring(index + 1, message.length());
		}
		
		if (("person").equals(protocol)) {
			
			String fromAccount = tmp.split("-")[1];
			friendsView.setChating(fromAccount, content);
		} else if ("group".equals(protocol)) {
			
			String fromName = tmp.split("-")[1];
			System.out.println(fromName + " : " + content);
		} else if (("friends").equals(protocol)) {
			
			Map<User, String> friendMap = new HashMap<>();
			String[] friends = content.split(":");
			for (int i = 1; i < friends.length; i++){
				
				String[] friendInformation = friends[i].split("-");
				User friend = new User();
				friend.setUser_account(friendInformation[0]);
				friend.setUser_name(friendInformation[1]);
				friend.setUser_tel(friendInformation[3]);
				friend.setUser_email(friendInformation[4]);
				friend.setUser_icon(friendInformation[5]);
				friend.setIsOnline(friendInformation[6]);
			
				String friendRemark = friendInformation[2];
				
				friendMap.put(friend, friendRemark);
			}
			
			friendsView.addFriends(friendMap);
		} else if (("logout").equals(protocol)){
			
			String logoutAccount = tmp;
			friendsView.setLogoutFriend(logoutAccount);
		} else if (("login").equals(protocol)){
			
			String loginAccount = tmp;
			friendsView.setLoginFriend(loginAccount);
		} else if ("verification".equals(protocol)) {
			
			MessageCache messageCache = new MessageCache();
			messageCache.setFrom_account(tmp.split("-")[1]);
			messageCache.setTo_account(selfAccount);
			
			index = content.lastIndexOf(":");
			messageCache.setContent(content.substring(0, index));
			messageCache.setMessage_type(Integer.parseInt(content.substring(index + 1, content.length())));
			MessageVerificationView messageVerificationView = new MessageVerificationView(messageCache, socketChannel);
			messageVerificationView.setLocationRelativeTo(null);
			messageVerificationView.setVisible(true);
		} else if ("searchperson".equals(protocol)) {
			
			friendsView.setSearchPerson(content);
		}
		else {
			System.out.println(message);
		}
	}

	public static void main(String[] args) {
		InetSocketAddress socketAddress = new InetSocketAddress("localhost", 6789);
		Client client = new Client(socketAddress);
		try {
			client.start();
		} catch (IOException e) {
			System.err.println("It's define to connect server");
			System.exit(0);
		}
	}
}
