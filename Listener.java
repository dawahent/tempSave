import java.net.*;
import java.io.*;
import java.util.ArrayList;
public class Listener extends Thread {
  private ServerSocket serverSocket;
  private GroupMember me; //element of memberlist with the specified id



  //constructor: specified position in memberList and binding to socket
  public Listener(int id){
    me = GroupMember.searchByMemberId(id);
    //binding to socket
    try{
      serverSocket = new ServerSocket(me.port);
    }catch(IOException e){
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
        System.out.println("kukukuku");
        //reading data from connections
        DataInputStream in = new DataInputStream(server.getInputStream());
        String rawMsg = in.readUTF();

        //The rawMsg could be:
        //reporting online status
        if(rawMsg.matches("o\\s.+")){
          int idToOnline = Integer.parseInt(rawMsg.substring(2,rawMsg.length()));
          GroupMember temp = GroupMember.searchByMemberId(idToOnline);
          //if didn't find, then exit with error
          if(temp == null){
            System.out.println("Mysterious guy reports for duty~~~");
            System.exit(1);
          }
          temp.online = true;
          System.out.println("Put online: id " + Integer.toString(idToOnline));
          continue;
        }

        //sending a unicast msg
        if(rawMsg.matches("u\\s.+\\s.+")){
          int secondSpaceIdx = rawMsg.indexOf(' ', 2);
          String whosend = rawMsg.substring(2, secondSpaceIdx);
          String msg = rawMsg.substring(secondSpaceIdx + 1, rawMsg.length());
          DelayThenBuff dfThread = new DelayThenBuff(msg, Integer.parseInt(whosend), false);
          dfThread.start();
          continue;
        }

        //sending a casual msg
        if(rawMsg.matches("c\\s.+")){
          //detect semicolon positions
          int scIdx = rawMsg.indexOf(';');
          String whosend = rawMsg.substring(2,scIdx);
          String msgWithV = rawMsg.substring(scIdx + 1, rawMsg.length());
          DelayThenBuff dfThread = new DelayThenBuff(msgWithV, Integer.parseInt(whosend), true);
          dfThread.start();
          continue;
        }

        //sending a total msg (to sequencer)
        if(rawMsg.matches("to.+")){
          // System.out.println('I am seq');
          System.out.println(rawMsg);
          int spaceIdx = rawMsg.indexOf(' ');
          String fetchIdx_Str = rawMsg.substring(2,spaceIdx);
          // System.out.println(fetchIdx_Str);
          int fetchIdx = Integer.parseInt(fetchIdx_Str);
          String fetchMsg = rawMsg.substring(spaceIdx + 1, rawMsg.length());
          DelayThenBuff dfThread = new DelayThenBuff(fetchMsg,fetchIdx,true);
          dfThread.run();
          continue;
        }

        //sending a total msg (to non-sequencers)
        if(rawMsg.matches("tm.+")){
          // System.out.println('I am not seq');
          System.out.println(rawMsg);

          String fetchMsg = rawMsg.substring(2, rawMsg.length());
          DelayThenBuff dfThread = new DelayThenBuff(fetchMsg,0,true);
          dfThread.run();
          continue;
        }

        //unknow message
        System.out.println("message not in formats: " + rawMsg);

        //close accpeter for new accpeter in next iter
        server.close();

      }catch(IOException e){
        e.printStackTrace();
      }


    }
  }
}
