import java.util.ArrayList;

public class BuffMsg{
  public String msg;
  public int senderIdx;
  //CO-multi:
  public int[] V;
  //Total-multi:
  public int S;
  public int delay;

  private BuffMsg(String msg, int senderIdx, int[] V, int delay){
    this.msg = msg;
    this.senderIdx = senderIdx;
    this.V = V;
    this.delay = delay;
  }

  private BuffMsg(String msg, int senderIdx, int S, int delay){
    this.msg = msg;
    this.senderIdx = senderIdx;
    this.S = S;
    this.delay = delay;
  }

  public static ArrayList<BuffMsg> MsgQ = new ArrayList<BuffMsg>();
  public static ArrayList<BuffMsg> OrderQ = new ArrayList<BuffMsg>();

  //warning: functions in BuffMsg are not atomic, wrap it with muteces in thread
  //CO-multi:
  //return true if DelayThenBuff.V has modified int this function
  public static boolean MsgEnq_C(String msg, int senderIdx, int[] V, int delay){
    //if only one elements of V is one more than the static V, and others >=
    if(DelayThenBuff.compV(V, senderIdx)){
      MyUtil.printMsgCasual(msg, senderIdx, delay, V);

      //after Delievering, increament the corresponding element in V
      DelayThenBuff.increV(senderIdx);
      return true;
    }
    BuffMsg temp = new BuffMsg(msg, senderIdx, V, delay);
    MsgQ.add(temp);
    return false;
  }

  //return true if following function increment V
  public static boolean AfterIncV_C(int senderIdx){
    for(int i = 0; i < MsgQ.size(); i++){
      BuffMsg temp = MsgQ.get(i);
      if(temp == null){
        continue;
      }
      if(DelayThenBuff.compV(temp.V, senderIdx)){
        MyUtil.printMsgCasual(temp.msg, temp.senderIdx, temp.delay, temp.V);
        MsgQ.remove(i);
        DelayThenBuff.increV(senderIdx);
        return true;
      }
    }
    return false;
  }

  //Total-multi
  public static boolean MsgEnq_T(String realMsg, int msgSenderIdx, int msgS, int delay){
    if(msgS == DelayThenBuff.S){
      if(msgSenderIdx != GroupMember.meIdx)
        MyUtil.printMsg(realMsg, msgSenderIdx, delay);

      DelayThenBuff.S++;
      return true;
    }
    BuffMsg temp = new BuffMsg(realMsg, msgSenderIdx, msgS, delay);
    MsgQ.add(temp);
    return false;
  }

  public static boolean AfterIncS(){
    for(int i = 0; i < MsgQ.size(); i++){
      BuffMsg temp = MsgQ.get(i);
      if(temp == null){
        continue;
      }
      if(temp.S == DelayThenBuff.S){
        if(temp.senderIdx != GroupMember.meIdx)
          MyUtil.printMsg(temp.msg, temp.senderIdx, temp.delay);
        MsgQ.remove(i);
        DelayThenBuff.S++;
        return true;
      }
    }
    return false;
  }
}
