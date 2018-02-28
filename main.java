import java.util.ArrayList;
import java.util.Scanner;
public class main {

    public static void main(String[] args) {
      //verify if args are valid
      if(args.length != 2){
        System.out.println("Number of args should be 2");
        System.out.println("arg 1 is 1 if total, 2 if casual");
        System.out.println("arg 2 is id in config file");
        return;
      }

      //parse args
      int methodIdx = Integer.parseInt(args[0]);
      int myPid = Integer.parseInt(args[1]);



      //launch receiptant before talking to others
      GroupMember.loadConfig(myPid);
      //init Delay and Buffer
      DelayThenBuff.init(methodIdx);
      Listener rawMessageReceiver = new Listener(myPid);
      rawMessageReceiver.start();

      //run talker in main thread,
      //ctor below will inform online processes online with its eligiblity
      Talker rawMessageSender = new Talker(methodIdx, myPid);

      //getting and run usr cmd
      System.out.println("Start your commands:");
      Scanner scanner = new Scanner( System.in );
      while(true){
        String input = scanner.nextLine();
        //once finished, let the talker parse it
        rawMessageSender.parseCmd(input);
      }
    }
}
