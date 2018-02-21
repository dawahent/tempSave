import java.util.ArrayList;
import java.io.*;
public class GroupMember {
  //four basic properties of a group member
  public int id;
  public String ip;
  public int port;
  public boolean online;

  //construcotr of GroupMember, set online to false at default
  public GroupMember(int assignedId, String assignedIP, int assignedPort){
    id = assignedId;
    ip = assignedIP;
    port = assignedPort;
    online = false;
  }

  //return an arraylist of group members, if the group member is current pid
  //set online to true
  public static ArrayList<GroupMember> loadConfig(int myPid){
    String fileName = "config";
    String line = null;

    ArrayList<GroupMember> res = new ArrayList<GroupMember>();

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
        }
        res.add(temp);

        lineNum++;
      }
    }
    catch(IOException ex){
      //if error, exit the whole program
      System.out.println("error loading config file");
      System.exit(1);
    }

    return res;
  }

  //return GroupMember from its arraylist if matched by id.
  //return null if not found
  public static GroupMember searchByMemberId(ArrayList<GroupMember> mb, int searchId){
    for(GroupMember i:mb){
      if(i.id == searchId){
        return i;
      }
    }
    return null;
  }
}
