package client;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import model.User;

public class FriendsView {

	private JFrame jFrame = null;
	private JPanel jContentPane = null;
	private JScrollPane scrollPane = null;
	private AddFriendView addFriendView = null;
	private String userAccount;
	private SocketChannel socketChannel;
	FriendListPane friendListPane;

	public FriendsView(String userAccount, SocketChannel socketChannel) {
		this.userAccount = userAccount;
		this.socketChannel = socketChannel;
	}

	public JFrame getJFrame() {
		
		if (jFrame == null) {
			jFrame = new JFrame();
			jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jFrame.setSize(230, 500);
			jFrame.setTitle("好友列表");
			jFrame.setContentPane(getJContentPane());
		}
		return jFrame;
	}

	private JScrollPane getScrollPane() {// 给添加好友的容器JPanel添加滚动条；
		if (scrollPane == null) {
			friendListPane = new FriendListPane(userAccount, socketChannel);
			scrollPane = new JScrollPane(friendListPane);
			// scrollPane.setBounds(20,5, -1, 600);
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);// 不显示水平滚动条；
		}
		return scrollPane;
	}

	private JPanel getJContentPane() {// 实例化底层的容器JPanel；
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getScrollPane(), BorderLayout.CENTER);
			JButton addFriendButton = new JButton("添加好友");
			
			addFriendButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					addFriendView = new AddFriendView(userAccount, socketChannel);
					addFriendView.setLocationRelativeTo(null);
					addFriendView.setVisible(true);
				}
			});
			jContentPane.add(addFriendButton, BorderLayout.SOUTH);
		}
		return jContentPane;
	}
	
	public void setChating(String account, String content){
		friendListPane.setChating(account, content);
	}
	
	public void addFriends(Map<User, String> friends) {
		friendListPane.addFriends(friends);
	}
	
	public void setLogoutFriend(String account){
		friendListPane.friendLogout(account);
	}
	
	public void setLoginFriend(String account){
		friendListPane.friendLogin(account);
	}
	
	public void setSearchPerson(String searchResult) {
		addFriendView.updateMessageTextArea(searchResult);
	}
}
