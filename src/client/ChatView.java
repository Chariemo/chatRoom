package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author colin
 */
@SuppressWarnings("serial")
public class ChatView extends javax.swing.JFrame {

    /**
     * Creates new form NewJFrame
     */
	private String userAccount;
	private String friendAccount;
	private SocketChannel socketChannel;
	private String icon;
	private String name;
	
	private String address = "localhost";
	private SocketChannel filesocketChannel;

	private Charset charset = Charset.forName("UTF-8");
	
    public ChatView(String userAccount, String friendAccount, SocketChannel socketChannel, String icon, String name, SocketChannel fileSocketChannel) {
    	this.socketChannel = socketChannel;
		this.icon = icon;
		this.userAccount = userAccount;
		this.friendAccount = friendAccount;
		this.name = name;
		this.filesocketChannel = fileSocketChannel;
        initComponents();
       
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        setTitle(userAccount + "'s chatRoom...");
        this.getRootPane().setDefaultButton(jButton1);

        jLabel1.setIcon(new ImageIcon(icon));

        jLabel2.setText(name);

        jButton1.setText("发送");
        jButton1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendLisener(e);
			}
		});

        jButton2.setText("文件");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jButton2MouseReleased(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setLineWrap(true);
        jTextArea1.setEditable(false);
        jScrollPane2.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addGap(99, 99, 99)
                        .addComponent(jButton2))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>                        

 // 发送文件
 	private void jButton2MouseReleased(java.awt.event.MouseEvent evt) {
 		
 		new sendFileThread().start();
// 		FileSelectView fileSelectView = new FileSelectView();
// 		fileSelectView.setVisible(true);
// 		fileSelectView.setLocationRelativeTo(null);
// 		fileSelectView.jFileChooser1.setMultiSelectionEnabled(true);
// 		fileSelectView.jFileChooser1.setDialogTitle("请选择要发送的文件...");
//		int returnVal = fileSelectView.jFileChooser1.showOpenDialog(null);
//		File file = null;
//		
//		if (JFileChooser.APPROVE_OPTION == returnVal) {
//			file = fileSelectView.jFileChooser1.getSelectedFile();
//			fileSelectView.dispose();
//		} else {
////			JOptionPane.showMessageDialog(null, "打开文件类型错误！");
//			fileSelectView.dispose();
//		}
//		if (filesocketChannel == null || !filesocketChannel.socket().isConnected()) {
//			try {
//				filesocketChannel = SocketChannel.open(new InetSocketAddress(address, 6790));
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		if (file != null) {
//			try {
//				String fileMessage = userAccount + "-" + friendAccount + "-" + file.getName();
//				int messageLengh = fileMessage.getBytes(charset).length;
//				buffer.clear();
//				buffer.putInt(messageLengh);
//				buffer.put(charset.encode(fileMessage).array(), 0, messageLengh);
//				buffer.flip();
//				buffer.rewind();
//				filesocketChannel.write(buffer);
//				buffer.clear();
//				FileChannel fileChannel = new FileInputStream(file).getChannel();	
//				while (fileChannel.read(buffer) > 0){
//					buffer.flip();
//					while (buffer.hasRemaining())
//						filesocketChannel.write(buffer);
//					buffer.clear();
//				}
//				filesocketChannel.close();
//				fileChannel.close();
////				filesocketChannel.socket().close();
//				System.out.println("hear");
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
 	}
 	
 	private class sendFileThread extends Thread {
 		
 		ByteBuffer buffer = ByteBuffer.allocate(1024);
 		
 		public void run() {
 			FileSelectView fileSelectView = new FileSelectView();
 	 		fileSelectView.setVisible(true);
 	 		fileSelectView.setLocationRelativeTo(null);
 	 		fileSelectView.jFileChooser1.setMultiSelectionEnabled(true);
 	 		fileSelectView.jFileChooser1.setDialogTitle("请选择要发送的文件...");
 			int returnVal = fileSelectView.jFileChooser1.showOpenDialog(null);
 			File file = null;
 			
 			if (JFileChooser.APPROVE_OPTION == returnVal) {
 				file = fileSelectView.jFileChooser1.getSelectedFile();
 				fileSelectView.dispose();
 			} else {
// 				JOptionPane.showMessageDialog(null, "打开文件类型错误！");
 				fileSelectView.dispose();
 			}
 			if (filesocketChannel == null || !filesocketChannel.socket().isConnected()) {
 				try {
 					filesocketChannel = SocketChannel.open(new InetSocketAddress(address, 6790));
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			if (file != null) {
 				try {
 					String fileMessage = userAccount + "-" + friendAccount + "-" + file.getName();
 					int messageLengh = fileMessage.getBytes(charset).length;
 					buffer.clear();
 					buffer.putInt(messageLengh);
 					buffer.put(charset.encode(fileMessage).array(), 0, messageLengh);
 					buffer.flip();
 					buffer.rewind();
 					filesocketChannel.write(buffer);
 					buffer.clear();
 					FileChannel fileChannel = new FileInputStream(file).getChannel();	
 					while (fileChannel.read(buffer) > 0){
 						buffer.flip();
 						while (buffer.hasRemaining())
 							filesocketChannel.write(buffer);
 						buffer.clear();
 					}
 					filesocketChannel.close();
 					fileChannel.close();
// 					filesocketChannel.socket().close();
 					System.out.println("hear");
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}

 	// 发送消息
 	private void sendLisener (ActionEvent e) {
 		
 		ByteBuffer buffer = ByteBuffer.allocate(1024);
 		String content = jTextField1.getText();
 		String message = userAccount + "-" + "person" + "-" + friendAccount + ":" + content;
 		int messageLength = message.getBytes(charset).length;
 		buffer.putInt(messageLength);
 		buffer.put(charset.encode(message).array(), 0, messageLength);
 		buffer.flip();
// 		System.out.println(buffer.getInt() + " " + charset.decode(buffer));
 		while (buffer.hasRemaining()) {
 			try {
 				socketChannel.write(buffer);
 			} catch (IOException ex) {
 				ex.printStackTrace();
 			}
 		}
 		jTextField1.setText("");
 		jTextField1.requestFocus();
 		buffer.clear();
 		showMessage(content);
 	}
 	
 	private void showMessage(String content) {
 		String tmp = "";
 		for (int i = 1; i < 57 - content.getBytes(charset).length; i++)
 			tmp += " ";
 		jTextArea1.append(tmp + content);
 		jTextArea1.append("\n");
 	}

 	public void receiveMessage(String content) {
 		String message = name + " : " + content;
 		jTextArea1.append(message);
 		jTextArea1.append("\n");
 	}                                     

    // Variables declaration - do not modify                     
    public javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration                   
}
    