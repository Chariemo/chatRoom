package client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import javax.swing.JOptionPane;

import model.User;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author colin
 */
public class ChangeUserInformation extends javax.swing.JFrame {

	/**
	 * Creates new form NewJFrame
	 */
	SocketChannel socketChannel;
	private Charset charset = Charset.forName("UTF-8");
	private User user;

	public ChangeUserInformation(SocketChannel socketChannel, User user) {
		this.socketChannel = socketChannel;
		this.user = user;
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		jMenuBar1 = new javax.swing.JMenuBar();
		jMenu1 = new javax.swing.JMenu();
		jMenu2 = new javax.swing.JMenu();
		jTabbedPane1 = new javax.swing.JTabbedPane();
		jPanel1 = new javax.swing.JPanel();
		jLabel2 = new javax.swing.JLabel();
		jButton2 = new javax.swing.JButton();
		jPasswordField1 = new javax.swing.JPasswordField();
		jLabel3 = new javax.swing.JLabel();
		jTextField2 = new javax.swing.JTextField();
		jLabel4 = new javax.swing.JLabel();
		jTextField3 = new javax.swing.JTextField();
		jLabel5 = new javax.swing.JLabel();
		jTextField4 = new javax.swing.JTextField();
		jLabel6 = new javax.swing.JLabel();
		jButton3 = new javax.swing.JButton();

		jMenu1.setText("File");
		jMenuBar1.add(jMenu1);

		jMenu2.setText("Edit");
		jMenuBar1.add(jMenu2);

		setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);

		jLabel2.setText("新密码：");

		jButton3.setText("选择图片");
		jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseReleased(java.awt.event.MouseEvent evt) {
				jButton3MouseReleased(evt);
			}
		});
		
		jButton2.setText("确定");
		jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseReleased(java.awt.event.MouseEvent evt) {
				jButton2MouseReleased(evt);
			}
		});
		
		jTextField2.setText(user.getUser_name());
		
		jTextField3.setText(user.getUser_tel());
		
		jTextField4.setText(user.getUser_email());
		
		jButton3.setText(user.getUser_icon());

		jLabel3.setText("昵称：");

		jLabel4.setText("手机：");

		jLabel5.setText("邮箱：");

		jLabel6.setText("头像");

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
						.addGap(0, 0, Short.MAX_VALUE)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jLabel2).addComponent(jLabel3).addComponent(jLabel4).addComponent(jLabel5)
								.addComponent(jLabel6))
						.addGap(46, 46, 46)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
								.addComponent(jButton3, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
								.addComponent(jTextField4, javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jTextField3, javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jTextField2, javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jPasswordField1, javax.swing.GroupLayout.Alignment.LEADING))
						.addGap(54, 54, 54))
				.addGroup(jPanel1Layout
						.createSequentialGroup().addGap(69, 69, 69).addComponent(jButton2,
								javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(83, Short.MAX_VALUE)));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup().addGap(28, 28, 28)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel3).addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGap(18, 18, 18)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 26,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGap(18, 18, 18)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel4).addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGap(18, 18, 18)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel5).addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGap(18, 18, 18)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel6).addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGap(26, 26, 26).addComponent(jButton2).addContainerGap(16, Short.MAX_VALUE)));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addGap(40, 40, 40)
						.addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(43, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addGap(24, 24, 24)
						.addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		pack();
	}// </editor-fold>
	
	private void jButton3MouseReleased(java.awt.event.MouseEvent evt) {
		java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                SelectIconView selectIconView = new SelectIconView(jButton3);
                selectIconView.setVisible(true);
                selectIconView.setLocationRelativeTo(null);
            }
        });
	}

	private void jButton2MouseReleased(java.awt.event.MouseEvent evt) {
		String passwd = jPasswordField1.getText();
		String name = jTextField2.getText();
		String tel = jTextField3.getText();
		String email = jTextField4.getText();
		String icon = jButton3.getText();
		ByteBuffer buffer = ByteBuffer.allocate(512);

		if (passwd.equals("") || name.equals("") || tel.equals("") || email.equals(""))
			JOptionPane.showMessageDialog(this, "密码、昵称、手机、邮箱不能为空！");
		else {
			String signUpMessage = user.getUser_account() + "-modifyuser-update-" + name + "-" + passwd + "-" + tel + "-" + email;
			
			if (icon.equals(""))
				signUpMessage += "- " + icon;
			else
				signUpMessage += "-" + icon;

			int signUpMessageLength = signUpMessage.getBytes(charset).length;
			buffer.putInt(signUpMessageLength);
			buffer.put(charset.encode(signUpMessage).array(), 0, signUpMessageLength);
			buffer.flip();
			System.out.println(charset.decode(buffer));
			buffer.flip();
			while (buffer.hasRemaining()){
				try {
					socketChannel.write(buffer);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			buffer.clear();
			this.dispose();
		}
	}

	// Variables declaration - do not modify
	private javax.swing.JButton jButton2;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JMenu jMenu1;
	private javax.swing.JMenu jMenu2;
	private javax.swing.JMenuBar jMenuBar1;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPasswordField jPasswordField1;
	private javax.swing.JTabbedPane jTabbedPane1;
	private javax.swing.JTextField jTextField2;
	private javax.swing.JTextField jTextField3;
	private javax.swing.JTextField jTextField4;
	private javax.swing.JButton jButton3;
	// End of variables declaration
}
