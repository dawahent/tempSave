import java.net.*;
import java.io.*;
import java.util.ArrayList;
public class Listener extends Thread {
  private ServerSocket serverSocket;
  private ArrayList<GroupMember> memberList;
  private GroupMember me; //element of memberlist with the specified id

  //constructor
  public Listener(int id, ArrayList<GroupMember> mb){
    memberList = mb;
    me = GroupMember.searchByMemberId(memberList, id);

    try{
      serverSocket = new ServerSocket(me.port);
    }catch(IOException e){
      e.printStackTrace();
      System.out.println("fail while creating serverSocket");
      System.exit(1);
    }

  }

  public void run(){
    System.out.println("waiting msg from other processes");
    while(true){


      //waiting for connections
      Socket server;
      try{
        server = serverSocket.accept();
        //reading data from connections
        DataInputStream in = new DataInputStream(server.getInputStream());
        String rawMsg = in.readUTF();

        //if it is about reporting online status, put it online
        if(rawMsg.charAt(0) == 'o' && rawMsg.charAt(1) == ' '){
          int idToOnline = Integer.parseInt(rawMsg.substring(2,rawMsg.length()));
          GroupMember temp = GroupMember.searchByMemberId(memberList, idToOnline);
          temp.online = true;
          System.out.println("Put online: id " + Integer.toString(idToOnline));
          continue;
        }

        //pass to delay if it is not about reporting online status
        System.out.println(rawMsg);

        //close accpeter for new accpeter in next iter
        server.close();

      }catch(IOException e){
        e.printStackTrace();
      }


    }
  }
}
