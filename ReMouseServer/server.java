import java.net.*;
import java.awt.AWTException;
import java.io.*;

public class server extends Thread
{
	 ServerSocket serverSocket;
	 Socket socket=null;
	 BufferedReader br;
	 String s="";
	 DataInputStream in;
	 DataOutputStream out;
	 remouse rem;
	 public  server(int port)throws IOException, AWTException
	 {
		 serverSocket=new ServerSocket(port);
		 serverSocket.setSoTimeout(10000);
		 rem=new remouse(); 
	 }
	 public void run()
	 {
		 while(socket==null)
		 {
			 try{
				 	 br=new BufferedReader(new InputStreamReader(System.in));
					 System.out.println("Waiting for client at port "+serverSocket.getLocalPort());
					 socket=serverSocket.accept();
					 System.out.println("Connected to "+ socket.getRemoteSocketAddress());
					 in=new DataInputStream(socket.getInputStream());
					 System.out.println(in.toString()+" "+in.readUTF());
					 out=new DataOutputStream(socket.getOutputStream());
					 
				 }
			 catch(Exception e)
			 {
					 System.out.println("Error: "+e.getMessage());
					// e.printStackTrace();
					 //return;
			 }
		 }
		 while(!s.equals("-1"))
		 {
			 try{
				String y=in.readUTF();
				if(y.length()!=0){
					
					if(y.equalsIgnoreCase("Mouse UP")){
						rem.getLocation();
					}
					else{
						 String x[]=y.split(" ");
						 if(x.length==2)
							 {
							 	//System.out.println(x[0]+" "+x[1]);
							 	rem.move(x);
							 }
						 else if(x.length==1)
							 rem.Click(Integer.parseInt(x[0]));
					}
					s=y;
					 //out.writeUTF("Closing server connection"+socket.getLocalAddress());
					 //return;
					 //serverSocket.close();
				}
			 }catch(Exception e)
			 {
				 System.out.println("Error: "+e.getMessage());
				 e.printStackTrace();
				 return;
			 }
		 }
		 if(socket!=null)
			try {
				System.out.print("Closing Socket");
				socket.close();
				serverSocket.close();
				return;
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		 
	 }
	 public static void main(String args[])throws IOException, AWTException
	 {
		 int port=8080;
		 Thread t=new server(port);
		 t.start();
	 }
}