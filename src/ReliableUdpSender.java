import java.io.*;
import java.net.*;
import java.util.*;

public class ReliableUdpSender {
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

        int ackNum = -1;
        while (++ackNum < MAX_PING_REQUEST) {
            // Cria Datagram de resposta do servidor
            DatagramPacket response = new DatagramPacket(new byte[1024], 1024);

            // Cria Timer
            Date date = new Date();
            long timestamp = date.getTime();

            // Cria mensagem que sera enviada ao servidor
            String sendMessage = ackNum + ";Mensagem de ACK " + ackNum + " \r\n";

            // Converte msg para array de bytes
            byte[] buffer = new byte[1024];
            buffer = sendMessage.getBytes();

            // Envia datagram para o servidor
            DatagramPacket pingRequest = new DatagramPacket(buffer, buffer.length, serverHost, serverPort);
            socket.send(pingRequest);

            // Receber resposta do servidor
            try {
                // Tenta receber o pacote do servidor
                socket.receive(response);

                if(ackNum == printData(response)){
                    System.out.println("Pacote de ACK " + ackNum + " confirmado");
                }
            }
            catch (SocketTimeoutException e) {
                System.out.println("Pacote de ACK" + ackNum + " perdido. Tentando enviar novamente");
                ackNum--;
            }
        }
    }

    /*
    * Print ping data to the standard output stream.
    */
    private static int printData(DatagramPacket request) throws Exception {
        
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
        
        String rawMessage = new String(line);
        int ackNum = Integer.parseInt(rawMessage.split(";")[0]); 
        String content = rawMessage.split(";")[1];
        
        // Print host address and data received from it.
        //System.out.println("Received from " + request.getAddress().getHostAddress() + ": " + content);

        return ackNum;
    }
}