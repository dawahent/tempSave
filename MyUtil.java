import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

class MyUtil {
  private static DateFormat df = new SimpleDateFormat("HH:mm:ss");
  private static Calendar calobj = Calendar.getInstance();

  public static void printMsg(String msg, int senderIdx, int delay){
    GroupMember sender = GroupMember.memberList.get(senderIdx);
    String senderIdStr = Integer.toString(sender.id);
    String delayMsg = ", Delay is " + Integer.toString(delay) + " ms";
    System.out.println("Received \"" + msg + "\" from process " + senderIdStr +
    ", Delay is " + Integer.toString(delay) +
    ", system time is " + df.format(calobj.getTime()));
  }

  public static void printMsgCasual(String msg, int senderIdx, int delay, int[] V){
    GroupMember sender = GroupMember.memberList.get(senderIdx);
    String senderIdStr = Integer.toString(sender.id);
    String delayMsg = ", Delay is " + Integer.toString(delay) + " ms";
    String VMsg = ", stamp is " + stringifyV(V);
    System.out.println("Received \"" + msg + "\" from process " + senderIdStr
    + delayMsg + VMsg + ", system time is " + df.format(calobj.getTime()));
  }

  //following msg print sent by pid (string)
  public static void printSentMsg(String msg, String pid){
    System.out.println("Sent \"" + msg + "\" to process " + pid +
    ", system time is " + df.format(calobj.getTime()));
  }

  private static String stringifyV(int[] V){
    String res = "";
    for(int i:V){
      res = res + Integer.toString(i) + " ";
    }
    return res;
  }

}
