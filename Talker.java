import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
public class Talker {

  //method index: 1 => Casual Ordering, 2 => total Ordering
  private int methodIdx;
  private int myPid;

  //Talker ctor
  public Talker(int met, int id){
    methodIdx = met;
    myPid = id;
    //report you are alive
    for(GroupMember i:GroupMember.memberList){
      if(i.id != myPid){
        i.online = unicast(i.id, "o "+Integer.toString(myPid));
        if(i.online){
          System.out.println("Put online: id " + Integer.toString(i.id));
        }
      }
    }
  }

  //parse the command from the users and exec the cmd if valid
  public void parseCmd(String usrCmd){
    //if usrCmd is 'send x y', unicast x with msg of 'u 'myPid' 'y
    if(usrCmd.matches("send\\s.+\\s.+")){
      int secondSpaceIdx = usrCmd.indexOf(' ', 5);
      int idToSend = Integer.parseInt(usrCmd.substring(5, secondSpaceIdx));
      String msgToSend = "u " + Integer.toString(GroupMember.meIdx) + " ";
      msgToSend += usrCmd.substring(secondSpaceIdx + 1, usrCmd.length());
      if(unicast(idToSend, msgToSend)){
        MyUtil.printSentMsg( usrCmd.substring(secondSpaceIdx + 1, usrCmd.length()),
        Integer.toString(idToSend));
      }
      return;
    }
    //if usrCmd is 'msend y', multicast p's in group with:
    //total: 'tm'myPid' 'y
    //casual: 'c 'myPid' 'V' 'y
    if(usrCmd.matches("msend\\s.+")){
      String msgToSend = "";
      if(methodIdx == 1){
        //total
        if(GroupMember.meIdx == 0){
          try{
            //if I am sequencer (meIdx == 0):
            //grab the knock
            //create msg: "tm" + "idx S msg"
            //S++
            //put down the knock
            DelayThenBuff.mutMsgQ.acquire();
            msgToSend = "tm0" + " " + Integer.toString(DelayThenBuff.S) + " ";
            msgToSend = msgToSend + usrCmd.substring(6, usrCmd.length());
            DelayThenBuff.S++;
            DelayThenBuff.mutMsgQ.release();
          }catch(InterruptedException ex){
            System.out.println(ex);
            System.exit(1);
          }
        }else{
          //if I am not sequencer
          //report error if id0 not online
          //create msg: "to" + "idx msg", unicast to sequencer
          //return now to avoid group sending these msg's
          GroupMember temp = GroupMember.memberList.get(0);
          if(!temp.online){
            System.out.println("sequencer is not online");
            return;
          }
          msgToSend = "to" + Integer.toString(GroupMember.me.id);
          msgToSend = msgToSend + " " + usrCmd.substring(6, usrCmd.length());
          unicast(temp.id,msgToSend);
          return;
        }



      }else{
        //casual
        try{
          DelayThenBuff.mutMsgQ.acquire();
          DelayThenBuff.increV(GroupMember.meIdx);
          DelayThenBuff.mutMsgQ.release();
        }
        catch(InterruptedException ex){
          System.out.println(ex);
          System.exit(1);
        }
        msgToSend = "c " + Integer.toString(GroupMember.meIdx) + ";";
        msgToSend += DelayThenBuff.stringV();
        msgToSend += ";" + usrCmd.substring(6, usrCmd.length());
      }

      //group send msg (B-multicast)
      for(GroupMember i:GroupMember.memberList){
        if(i.online && i.id != GroupMember.me.id){
          unicast(i.id,msgToSend);
          System.out.println("fla");
        }
      }
      return;
    }

    //unknown User command: report it
    System.out.println("uknown user command");
  }

  //unicast raw msg, if cast success return true, otherwise false
  public static boolean unicast(int destPid, String msg){
    //warning: this function does not care if destPid is online!
    GroupMember temp = GroupMember.searchByMemberId(destPid);

    //temp is null then exit with error
    if(temp == null){
      System.out.println("pid of " + Integer.toString(destPid) + " is not in the list");
      System.exit(1);
    }
    try {
      Socket client = new Socket(temp.ip, temp.port);

      //sending message
      OutputStream outToServer = client.getOutputStream();
      DataOutputStream out = new DataOutputStream(outToServer);
      out.writeUTF(msg);
      client.close();
      return true;
    } catch (IOException e) {
      System.out.println("no online report back from " + Integer.toString(destPid));
      return false;
    }
  }

}
