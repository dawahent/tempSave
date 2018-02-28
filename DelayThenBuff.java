import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.Dictionary;
import java.io.*;
import java.lang.*;
import java.util.Random;

public class DelayThenBuff extends Thread {
  //for delay
  private static int delay0 = 0;
  private static int delayRange = 0;

  //muteces
  public static Semaphore mutMsgQ = new Semaphore(1);
  private static Semaphore mutRg = new Semaphore(1);
  private static Semaphore mutV = new Semaphore(1);

  //method index, 1: total, 2: casual
  private static int methodIdx;

  //order number for total ordering method
  public static int S;

  //timestamp vector for casual ordering method
  public static int[] V;

  //convert strings to int[]
  public static int[] parseVString(String vStr){
    String[] temp = vStr.split(" ");
    int[] res = new int[temp.length];
    for(int i = 0; i < temp.length; i++){
      res[i] = Integer.parseInt(temp[i]);
    }
    return res;
  }

  //increment the V[meIdx] before CO-multicasting
  public static int[] increV(int j){
    V[j] += 1;
    return V;
  }

  //helper function for comparing V:
  //true iff nV[j] = V[j] + 1 and nV[k] <= V[k] for k != j
  public static boolean compV(int[] nV, int j){
    //check validity of nV
    if(nV.length != V.length){
      System.out.println("the comming timestamp has different length");
      System.exit(1);
    }

    //comparison
    for(int i = 0; i < V.length; i++){
      if(i != j){
        if(!(nV[i] <= V[i]))
          return false;
      }else{
        if(!(nV[i] == V[i] + 1))
          return false;
      }
    }
    return true;
  }

  //helper function for stringify V:
  public static String stringV(){
    String res = "";
    for(int i = 0; i < V.length - 1; i++){
      res += Integer.toString(V[i]) + " ";
    }
    res += Integer.toString(V[V.length - 1]);
    return res;
  }

  //check if S matches any msg Buffed, if so

  //run below in main.java before buffering msg
  //warning: the initalization below assumes that GroupMember has been init
  public static void init(int met){
    if(met != 1 && met != 2){
      System.out.println("wrong method index, first argument should be either 1 or 2");
    }
    if(met == 1){
      S = 0;
      System.out.println("total multicast is on");
    }else{
      System.out.println("casual multicast is on");
    }
    methodIdx = met;

    //initalize V according to member list in GroupMember (no matter on/offline)
    //only comparing V's elements of onine processes later
    V = new int[GroupMember.memberList.size()];
    for(int i = 0; i < V.length; i++)
      V[i] = 0;

    //loading delays
    try{
      String fileName = "config";
      FileReader fileReader = new FileReader(fileName);
      BufferedReader bufferedReader = new BufferedReader(fileReader);
      String line = bufferedReader.readLine();
      int spaceIdx = line.indexOf(' ');
      delay0 = Integer.parseInt(line.substring(0,spaceIdx));
      int delay1 = Integer.parseInt(line.substring(spaceIdx + 1, line.length()));
      delayRange = delay1 - delay0;
    }catch(IOException e){
      System.out.println("fail while loading delay from config");
      System.exit(1);
    }
  }

  //unique to each thread (i.e. non-static)
  private String msg;
  private int senderMeIdx;
  private boolean ifmulticast;

  //ctor
  public DelayThenBuff(String msg, int senderMeIdx, boolean ifmulticast){
    this.msg = msg;
    this.senderMeIdx = senderMeIdx;
    this.ifmulticast = ifmulticast;
  }

  //rand seed
  private Random rand = new Random();

  //what to run in the thread:
  public void run(){
    int sleepMs = rand.nextInt(delayRange + 1) + delay0;
    try {
      // thread to sleep
      Thread.sleep(sleepMs);
      GroupMember temp = GroupMember.memberList.get(senderMeIdx);
      String sendPid = Integer.toString(temp.id);
      //if unicast, just sleep then wake
      if(!ifmulticast){
        System.out.println("Received \"" + this.msg + "\" from process " + sendPid);
        System.out.println(sleepMs);
        return;
      }

      //multicast msg:
      if(methodIdx == 1){
        //total
        if(senderMeIdx != 0){
          //I am the sequencer because I got msg from others
          //I am a sequencer
          //fetch idx and msg
          //grab the knock
          //create msg: "tm" + "idx S msg"
          //S++
          //print the msg
          //put down the knock
          //group send such msg's to others;
          try{
            mutMsgQ.acquire();
            MyUtil.printMsg(msg, senderMeIdx, sleepMs);
            String msgToSend = "tm" + Integer.toString(senderMeIdx) + " " + Integer.toString(S);
            msgToSend = msgToSend + " " + msg;
            S++;
            mutMsgQ.release();

            for(GroupMember i:GroupMember.memberList){
              if(i.online && i.id != GroupMember.me.id){
                Talker.unicast(i.id,msgToSend);
              }
            }
          }catch(InterruptedException ex){
            System.out.println(ex);
            System.exit(1);
          }
        }else{
          //I am not sequencer because I got msg from sequencer
          //fetch idx, S and Msg
          //grab the knock
          //if myS == S
          //print msg , S++
          //check MsgQ to see if new msg should print, henced S++
          //if so, recheck MsgQ to see if new msg should print, henced S++

          //if myS != S
          //put it to the MsgQ
          //put down the knock
          try{
            int firstSpaceIdx = msg.indexOf(' ');
            int secondSpaceIdx = msg.indexOf(' ', firstSpaceIdx + 1);
            String idxStr = msg.substring(0, firstSpaceIdx);
            String sStr = msg.substring(firstSpaceIdx + 1, secondSpaceIdx);
            String realMsg = msg.substring(secondSpaceIdx + 1, msg.length());

            int msgS = Integer.parseInt(sStr);
            int msgSenderIdx = Integer.parseInt(idxStr);

            // System.out.println("S: " + sStr);
            // System.out.println("from: " + msgSenderIdx);

            mutMsgQ.acquire();
            if(BuffMsg.MsgEnq_T(realMsg, msgSenderIdx, msgS, sleepMs)){
              while(BuffMsg.AfterIncS()){

              }
            }

            mutMsgQ.release();
          }catch(InterruptedException ex){
            System.out.println(ex);
            System.exit(1);
          }
        }

      }else{
        //casual
        int scIdx = msg.indexOf(';');
        String msgToQ = msg.substring(scIdx + 1, msg.length());
        String vString = msg.substring(0, scIdx);
        mutMsgQ.acquire();
        boolean vChangeLast = BuffMsg.MsgEnq_C(msgToQ, senderMeIdx, parseVString(vString),sleepMs);
        mutMsgQ.release();
        while(vChangeLast){
          mutMsgQ.acquire();
          vChangeLast = BuffMsg.AfterIncV_C(senderMeIdx);
          mutMsgQ.release();
        }
      }
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
