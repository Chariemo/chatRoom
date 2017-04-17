package client;

import javax.swing.ImageIcon;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JButton;
import javax.swing.JFrame;

import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.channels.SocketChannel;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;

public class MemberModel {
	private static final long serialVersionUID = 1L;
	public JButton jButton = null;// 显示好友头像；
	public JPanel jPanel = new JPanel();// 模板容器；
	
	private JLabel lb_nickName = null;// 显示昵称；
	private String icon;
	private String nickname = null;
	public JLabel lb_online = null;// 是否在线

	private boolean isOnline;
	private boolean isChating;
	
	private String friendAccount;
	private String userAccount;
	private SocketChannel socketChannel;
	
	private JPopupMenu pop;
	private JMenuItem item1;
	
	private ChatView chatView;

	public MemberModel(String icon, String nickname, int len, boolean isOnline, String userAccount,String friendAccount, SocketChannel socketChannel) {
		super();
		this.icon = icon;
		this.nickname = nickname;// 昵称；
		this.friendAccount = friendAccount;
		this.isOnline = isOnline;
		this.userAccount = userAccount;
		this.socketChannel = socketChannel;
		initialize();
	}

	private void initialize() {
		
		lb_online = new JLabel();
		lb_online.setBounds(new Rectangle(51, 30, 131, 20));
		lb_online.setFont(new Font("Dialog", Font.PLAIN, 12));
		if (isOnline)
			lb_online.setText("在线");
		else
			lb_online.setText("离线");
		lb_online.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent e) {
				exchangeEnter();
				lb_online.setToolTipText(lb_online.getText());
			}

			public void mouseExited(java.awt.event.MouseEvent e) {
				exchangeExited();
			}
			
			public void mouseClicked(MouseEvent e) {
				startChat(e);
			}

		});
		
		lb_nickName = new JLabel();
		lb_nickName.setBounds(new Rectangle(52, 10, 80, 20));
		lb_nickName.setFont(new Font("Dialog", Font.BOLD, 14));
		lb_nickName.setText(nickname);
		jPanel.setSize(new Dimension(200, 67)); // 每个用户所占大小
		jPanel.setLayout(null);
		jPanel.add(getJButton(), null);
		jPanel.add(lb_nickName, null);
		jPanel.add(lb_online, null);
		jPanel.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseExited(java.awt.event.MouseEvent e) {
				exchangeExited();// 鼠标移出模板区，改变背景颜色；
			}

			public void mouseEntered(java.awt.event.MouseEvent e) {
				exchangeEnter();// 鼠标移进模板区，改变背景颜色；
			}
			
			public void mouseClicked(MouseEvent e) {
				startChat(e);
			}
		});
		
		pop = new JPopupMenu();
		item1 = new JMenuItem("功能一");
		pop.add(item1);
		item1.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				System.out.println("item 1");// 输出按钮的文本
			}
		});
		jPanel.setComponentPopupMenu(pop);
		lb_online.setComponentPopupMenu(pop);
		lb_nickName.setComponentPopupMenu(pop);
		jButton.setComponentPopupMenu(pop);
	}

	private void exchangeEnter() {
		jPanel.setBackground(new Color(192, 224, 248));
	}

	private void exchangeExited() {
		jPanel.setBackground(null);
	}

	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setBounds(new Rectangle(8, 10, 40, 40));
			jButton.setBackground(new Color(236, 255, 236));
			jButton.setIcon(new ImageIcon(icon));
			jButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseExited(java.awt.event.MouseEvent e) {
					exchangeExited();// 鼠标移出模板区，改变背景颜色；
				}

				public void mouseEntered(java.awt.event.MouseEvent e) {
					exchangeEnter();// 鼠标移进模板区，改变背景颜色；
				}
				
				public void mouseClicked(MouseEvent e) {
					startChat(e);
				}
			});

		}
		return jButton;
	}

	public void changeOnlineStatus(boolean status) {
		isOnline = status;
		if (isOnline)
			lb_online.setText("在线");
		else
			lb_online.setText("离线");
	}
	
	public void setChating(){
		if (isChating){
			chatView.show();
		} else {
			chatView = new ChatView(userAccount, friendAccount, socketChannel, icon, nickname);
			chatView.setVisible(true);
            chatView.setLocationRelativeTo(null);
     //       chatView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			isChating = true;
		}
	}
	
	public void receiveMessage(String content) {
		chatView.receiveMessage(content);
	}
	
	private void startChat(MouseEvent e) {
	    if (e.getClickCount()==2){
	    	java.awt.EventQueue.invokeLater(new Runnable() {
	            public void run() {
	            	setChating();
	            }
	        });
	    }
	}

}