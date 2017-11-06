import java.io.*;
import java.net.*;
import java.util.*;

public class PingClient2 {
    private static final int TIMEOUT = 1000; // milliseconds
    private static final int MAX_PING_REQUEST = 10; // Numero de ping requests
    private static final int CLIENT_PORT = 5000;
    private static InetAddress serverHost = null;
    private static int serverPort = 0;
    private static DatagramSocket socket = null;

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.out.println("Required arguments: host port");
            return;
        }

        // Recupera host e porta
        serverHost = InetAddress.getByName(args[0]);
        serverPort = Integer.parseInt(args[1]);

        // Create a datagram socket for receiving and sending UDP packets
        // through the port specified on the command line.
        socket = new DatagramSocket(CLIENT_PORT);
        socket.setSoTimeout(TIMEOUT);

        // Request ping a cada 1 segundo
        Timer timer = new Timer();
        RemindTask remindTask = new RemindTask(MAX_PING_REQUEST, socket, serverHost, serverPort);
        timer.schedule(remindTask, 0, 1000);
    }
    
    /*
    * Request/Reply ping.
    */
    public static void ping(DatagramSocket socket, int sequence_number, InetAddress serverHost, int serverPort) {
        // Cria Datagram de resposta do servidor
        DatagramPacket response = new DatagramPacket(new byte[1024], 1024);

        // Cria Timer
        Date date = new Date();
        long timestamp = date.getTime();

        // Cria mensagem que sera enviada ao servidor
        String sendMessage = "PING " + sequence_number + " " + timestamp + " \r\n";

        // Converte msg para array de bytes
        byte[] buffer = new byte[1024];
        buffer = sendMessage.getBytes();

        // Envia datagram para o servidor
        DatagramPacket pingRequest = new DatagramPacket(buffer, buffer.length, serverHost, serverPort);

        try {
            socket.send(pingRequest);

            // Tenta receber o pacote do servidor
            socket.receive(response);

            // Calcula tempo de resposta
            date = new Date();
            long delayReceived = date.getTime() - timestamp;

            System.out.print("Delay " + delayReceived + " ms: ");
            printData(response);
        } catch (SocketTimeoutException e) {
            System.out.print("Pacote perdido: " + sendMessage);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    * Print ping data to the standard output stream.
    */
    private static void printData(DatagramPacket request) throws Exception {

        // Obtain references to the packet's array of bytes.
        byte[] buf = request.getData();

        // Wrap the bytes in a byte array input stream,
        // so that you can read the data as a stream of bytes.
        ByteArrayInputStream bais = new ByteArrayInputStream(buf);

        // Wrap the byte array output stream in an input stream reader,
        // so you can read the data as a stream of characters.
        InputStreamReader isr = new InputStreamReader(bais);

        // Wrap the input stream reader in a bufferred reader,
        // so you can read the character data a line at a time.
        // (A line is a sequence of chars terminated by any combination of \r and \n.)
        BufferedReader br = new BufferedReader(isr);

        // The message data is contained in a single line, so read this line.
        String line = br.readLine();

        // Print host address and data received from it.
        System.out.println("Received from " + request.getAddress().getHostAddress() + ": " + new String(line));
    }
}

class RemindTask extends TimerTask {
    private int maxPingRequests;
    private int times = -1;
    private DatagramSocket socket;
    private InetAddress serverHost;
    private int serverPort;
    

    public RemindTask(int maxPingRequests, DatagramSocket socket, InetAddress serverHost, int serverPort) {
        this.maxPingRequests = maxPingRequests;
        this.socket = socket;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public void run() {
        if (++this.times < this.maxPingRequests) {
            PingClient2.ping(this.socket, this.times, this.serverHost, this.serverPort);
        } else {
            System.exit(0);
        }
    }
}