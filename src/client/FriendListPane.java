package client;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import model.User;

import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;

public class FriendListPane extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel listName = null;
	private JLabel friendLabel = null;
	private int clickF = 0;

	private String userAccount;
	private Map<User, JLabel> userLabelMap = new HashMap<>();
	private Map<String, MemberModel> friendsMap = new HashMap<>();
	private SocketChannel socketChannel;
	

	public FriendListPane(String userAccount, SocketChannel socketChannel) {
		super();
		this.userAccount = userAccount;
		this.socketChannel = socketChannel;
	}

	private void initialize() {

		listName = new JLabel();
		listName.setText("我的好友");
		// listName.setIcon(new ImageIcon("icon/ico.jpg"));
		listName.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		listName.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent e) {
				clickF += 1;
				if (clickF % 2 == 1) {
					for (User user : userLabelMap.keySet()) {
						userLabelMap.get(user).setVisible(false);
					}
					// listName.setIcon(new ImageIcon("icon/ico2.jpg"));
					update();
				} else {
					for (User user : userLabelMap.keySet()) {
						userLabelMap.get(user).setVisible(true);
					}
					// listName.setIcon(new ImageIcon("icon/ico.jpg"));
					update();
				}
			}
		});

		// addJLabel();
	}

	private void update() {// 更新UI界面；
		this.updateUI();
	}
	
	public void addFriends(Map<User, String> friends) {
		initialize();
		this.add(listName, null);
		
		String friendName;
		for (User friend : friends.keySet()) {
			if (friends.get(friend).equals(""))
				friendName = friend.getUser_name();
			else
				friendName = friends.get(friend);
			friendLabel = new JLabel();
			MemberModel memberModel;
			friendLabel.setIcon(new ImageIcon("icon/bg.jpg"));
			 if (friend.getIsOnline().equals("online")){
				 memberModel = new MemberModel(friend.getUser_icon(), friendName, 200, true, userAccount, friend.getUser_account(), socketChannel);
				 friendLabel.add(memberModel.jPanel);
			 } else {
				 memberModel = new MemberModel(friend.getUser_icon(), friendName, 200, false, userAccount, friend.getUser_account(), socketChannel);
				 friendLabel.add(memberModel.jPanel);
			 }
			 
			friendsMap.put(friend.getUser_account(), memberModel);
			friendLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			userLabelMap.put(friend, friendLabel);
			this.add(friendLabel, null);
		}
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setSize(200, 408);
		this.setLocation(20, 5);
	}

	public void setChating(String friendAccount, String content) {
		friendsMap.get(friendAccount).setChating();
		friendsMap.get(friendAccount).receiveMessage(content);
	}

	public void friendLogout(String friendAccount) {
		if (friendsMap.containsKey(friendAccount))
			friendsMap.get(friendAccount).changeOnlineStatus(false);
	}

	public void friendLogin(String friendAccount) {
		if (friendsMap.containsKey(friendAccount))
			friendsMap.get(friendAccount).changeOnlineStatus(true);
	}

}
