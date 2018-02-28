import java.util.ArrayList;
import java.io.*;
public class GroupMember {
  //four basic properties of a group member
  public int id;
  public String ip;
  public int port;
  public boolean online;

  //memberList
  public static ArrayList<GroupMember> memberList;

  //useful static variable for locating self in the memberList
  public static GroupMember me;
  public static int meIdx;

  //construcotr of GroupMember, set online to false at default
  private GroupMember(int assignedId, String assignedIP, int assignedPort){
    id = assignedId;
    ip = assignedIP;
    port = assignedPort;
    online = false;
  }

  //return an arraylist of group members, if the group member is current pid
  //set online to true. Also, assign reference to me and meIdx
  public static void loadConfig(int myPid){
    String fileName = "config";
    String line = null;

    memberList = new ArrayList<GroupMember>();

    try{
      //loading the file
      FileReader fileReader = new FileReader(fileName);
      BufferedReader bufferedReader = new BufferedReader(fileReader);

      int lineNum = 0;
      while((line = bufferedReader.readLine()) != null) {
        //only parsing after first line
        if(lineNum == 0){
          lineNum++;
          continue;
        }

        String[] words = line.split("\\s", 0);
        int id_ = Integer.parseInt(words[0]);
        int port_ = Integer.parseInt(words[2]);
        
        GroupMember temp = new GroupMember(id_, words[1], port_);
        if(myPid == id_){
          temp.online = true;
          me = temp;
          meIdx = lineNum - 1;
        }
        memberList.add(temp);

        lineNum++;
      }
    }
    catch(IOException ex){
      //if error, exit the whole program
      System.out.println("error loading config file");
      System.exit(1);
    }
  }

  //return GroupMember from its arraylist if matched by id.
  //return null if not found
  public static GroupMember searchByMemberId(int searchId){
    for(GroupMember i:memberList){
      if(i.id == searchId){
        return i;
      }
    }
    return null;
  }
}
