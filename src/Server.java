import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server(){
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while(!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);

                pool.execute(handler);
            }
        }
        catch (Exception e) {
            shutdown();
        }
    }

    public void broadcast(String message) {
        for (ConnectionHandler ch : connections) {
            if (ch != null) {
                ch.SendMessage(message);
            }
        }
    }

    public void shutdown(){
        try {
            done = true;
            if(!server.isClosed()){
                server.close();
            }

            for(ConnectionHandler ch : connections){
                ch.shutdown();
            }
        }
        catch (IOException e){
            // Ignore
        }

    }

    /**
     * Class: ConnectionHandler
     * Allows multiple client connections.
     * */
    class ConnectionHandler implements Runnable{

        private Socket client;
        private BufferedReader in; // Get from client
        private PrintWriter out; // write to client

        private String nickname;
        // Constructor: ConnectionHandler
        // Each client will have their own connection handlers.
        public ConnectionHandler(Socket Client){
            client = Client;
        }


        @Override
        public void run(){
            try{
                out  = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                out.println("Enter your nickname: ");
                nickname = in.readLine();

                System.out.println(STR."\{nickname} connected.");
                broadcast(STR."\{nickname} joined the chat");

                String message;
                while((message = in.readLine()) != null){

                    if(message.startsWith("/nick ")){
                        // allows users to change their nickname.
                        String[] msgSplit;
                        msgSplit = message.split(" ", 2);

                        if(msgSplit.length == 2){
                            broadcast(STR."\{nickname} morphed into \{msgSplit[1]}");
                            System.out.println(STR."\{nickname} so forth will be known as \{msgSplit[1]}");
                            nickname = msgSplit[1];
                            out.println(STR."Nickname changed successfully to \{nickname}");
                        }
                        else {
                            out.println("No new nickname was given.");
                        }
                    }
                    else if(message.startsWith("/quit")){
                        broadcast(STR."\{nickname}, left the chat :(");
                        shutdown();
                    }
                    else{
                        broadcast(STR."\{nickname}: \{message}");
                    }
                }

            }
            catch (IOException e){
                shutdown();
            }

        }

        public void SendMessage(String message){
            out.println(message);
        }

        public void shutdown(){
            try{
                in.close();
                out.close();

                if(!client.isClosed()){
                    client.close();
                }
            }
            catch (IOException e){
                //Ignore
            }

        }
    }

    public static void main(String[] args){
        Server server = new Server();
        server.run();
    }

}
