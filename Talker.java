import java.net.*;
import java.io.*;
import java.util.ArrayList;
public class Talker {

  //method index: 1 => Casual Ordering, 2 => total Ordering
  private int methodIdx;
  private int myPid;
  private ArrayList<GroupMember> memberList;

  //Talker ctor
  public Talker(int met, int id, ArrayList<GroupMember> mb){
    methodIdx = met;
    myPid = id;
    memberList = mb;
    //report you are alive
    for(GroupMember i:mb){
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
    //if usrCmd is 'msend y', multicast p's in group with:
    //total: 'tm'myPid' 'y
    //casual: 'c 'myPid' 'V' 'y

    //unknown User command: report it
    System.out.println("uknown user command");
  }

  //unicast raw msg, if cast success return true, otherwise false
  public boolean unicast(int destPid, String msg){
    //warning: this function does not care if destPid is online!
    GroupMember temp = GroupMember.searchByMemberId(this.memberList, destPid);

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
      System.out.println("fail to communicate with pid of " + Integer.toString(destPid));
      return false;
    }
  }

}
