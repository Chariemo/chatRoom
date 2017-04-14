package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;


public class Client {
	private Selector selector;
	private InetSocketAddress serverAddress;
	private SocketChannel socketChannel;
	
	private final int BLOCK = 1024;
	private final ByteBuffer sendBuffer = ByteBuffer.allocate(BLOCK);
	private final ByteBuffer receiveBuffer = ByteBuffer.allocate(BLOCK);
	
	private Charset charset = Charset.forName("UTF-8");
	private Scanner scanner = new Scanner(System.in);
	private static boolean isSignIn;
	
	public Client(InetSocketAddress socketAddress) {
		this.serverAddress = socketAddress;
	}
	
	private void socketChannelInfo() throws IOException{
		selector = Selector.open();
		socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
		socketChannel.register(selector, SelectionKey.OP_CONNECT);
		socketChannel.connect(serverAddress);
	}
	
	public void start() throws IOException{
		
		socketChannelInfo();
		Thread read = new Thread(new read());
		read.start();
		String line = "";
		while(!read.isInterrupted() && (line = scanner.nextLine()) != null){
			sendBuffer.putInt(line.getBytes().length);
			sendBuffer.put(charset.encode(line).array(), 0, line.getBytes().length);
			sendBuffer.flip();
			while(sendBuffer.hasRemaining()) {
				socketChannel.write(sendBuffer);
			}
			sendBuffer.clear();
		}
	}
	
	private class read implements Runnable {
		
		@Override
		public void run() {
			isSignIn = false;
			try {
				while (true){
					selector.select();
					for(SelectionKey selectionKey : selector.selectedKeys()){
						selector.selectedKeys().remove(selectionKey);
						if (selectionKey.isConnectable()){
							System.out.println("client connect");
							socketChannel = (SocketChannel) selectionKey.channel();
							if (socketChannel.isConnectionPending()){
								socketChannel.finishConnect();
								System.out.println("achieve connectionPending");
							}
						}
						else if (selectionKey.isReadable()){
							SocketChannel keyChannel = (SocketChannel)selectionKey.channel();
							
							keyChannel.socket().sendUrgentData(0);
							
							receiveBuffer.clear();
							if (isSignIn){
			//					System.out.println("signins");
								handleMessage(readMessage(keyChannel));
							}
							else{
								isSignIn = handleSigninMessage(readMessage(keyChannel));
								if (isSignIn == true)
									System.out.println("Already connect to server");
								else
									System.out.println("not connect to server");
							}
							selectionKey.interestOps(SelectionKey.OP_READ);
						}
					}
					socketChannel.register(selector, SelectionKey.OP_READ);
				}
			} catch (IOException e) {
				Thread.currentThread().interrupt();
				System.err.println("The server is error ");
			}
		}
	}
	
	private String readMessage(SocketChannel keyChannel){
		String line = "";
		try {
			int rlen = 0;
			receiveBuffer.position(0);
			receiveBuffer.limit(4);
			int tmp = 0;
			while(true){
//				System.out.println("while 1");
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
			while (true){
			//	System.out.println("whlie 2");
				rlen = keyChannel.read(receiveBuffer);
				if (rlen != 0 && rlen != -1){
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
//		System.out.println("line: " + line);
		return line;
	}
	
	private boolean handleSigninMessage(String message){
		boolean result = false;
		String protocol = message.split(":")[0];
		String content = message.split(":")[1];
		if (("signin").equals(protocol))
			if (("ok").equals(content))
				result = true;
			else
				result = false;
		return result;
	}
	
	private void handleMessage(String message){
		int index = message.indexOf(":");
		String content = message.substring(index + 1, message.length());
		String tmp = message.substring(0, index);
		String protocol = tmp.split("-")[0];
		if (("person").equals(protocol)){
			String fromName = tmp.split("-")[1];
			System.out.println(fromName + " : " + content);
		}
		else if ("group".equals(protocol)){
			String fromName = tmp.split("-")[1];
			System.out.println(fromName + " : " + content);
		}
		else {
			System.out.println(message);
		}
	}
	
	
	public static void main(String[] args){
		InetSocketAddress socketAddress = new InetSocketAddress("localhost", 6789);
		Client client = new Client(socketAddress);
		try {
			client.start();
		} catch (IOException e) {
			System.err.println("It's define to connect server");
		}
	}
}
