import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements  Runnable {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;

    @Override
    public void run() {
        try {
            Socket client = new Socket("127.0.0.1", 9999);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inHandle = new InputHandler();
            Thread t = new Thread(inHandle);
            t.start();

            String inMsg;
            while((inMsg = in.readLine()) !=null){
                System.out.println(inMsg);
            }

        } catch (Exception e) {
            shutdown();
        }
    }
    public void shutdown() {
        done = true;

        try{
            in.close();
            out.close();

            if(!client.isClosed()){
                client.close();
            }
        } catch (IOException e){
            // ignore
        }
    }

    class InputHandler implements Runnable{

        @Override
        public void run() {
            try{
                BufferedReader inRead = new BufferedReader(new InputStreamReader(System.in));

                while(!done){
                    String message = inRead.readLine();

                    if(message.equals("/quit")){
                        inRead.close();
                        shutdown();
                    }
                    else{
                        out.println(message);
                    }
                }
            } catch (IOException e){
                shutdown();
            }
        }
    }
    public static void main(String[] args){
        Client client = new Client();
        client.run();
    }

}