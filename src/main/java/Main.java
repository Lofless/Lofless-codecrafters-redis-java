import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private final static Map<String,String> map = new HashMap<>();
    private static String role = "master";

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 6379;
        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
            if(args[i].equals("--replicaof")){
                role = "slave";
            }
        }
        try {
            // 创建一个服务端套接字
            serverSocket = new ServerSocket(port);
            // 重复使用本地端口和ip地址
            serverSocket.setReuseAddress(true);

            while (true) {
                // accept函数 -> 进入阻塞，等待客户端连接
                clientSocket = serverSocket.accept();
                // 创建多线程
                Socket finalClientSocket = clientSocket;
                new Thread(() -> {
                    try {
                        // 打印客户端连接信息
                        System.out.println("New client connected: " +
                                finalClientSocket.getInetAddress());
                        // 获取数据并运行代码
                        inputIoAndChooseCommandToRun(finalClientSocket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }

    // 读取数组
    public static void inputIoAndChooseCommandToRun(Socket clientSocket) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String line = null;
        while((line  = reader.readLine()) != null){
            System.out.println("input: " + line);
            if(line.toLowerCase().contains("ping")){
                clientSocket.getOutputStream().write("+PONG\r\n".getBytes());
            }
            else if (line.toLowerCase().contains("echo")) {
                reader.readLine();
                line = reader.readLine();
                clientSocket.getOutputStream().write(("+"+line+"\r\n").getBytes());
            }
            else if (line.toLowerCase().contains("set")){
                reader.readLine();
                String key = reader.readLine();
                System.out.println("key: " + key);
                reader.readLine();
                String value = reader.readLine();
                System.out.println("value: " + value);
                map.put(key,value);
                clientSocket.getOutputStream().write("+OK\r\n".getBytes());
                // expire time
                reader.readLine();
                String type = reader.readLine();
                if("px".equalsIgnoreCase(type)){
                    reader.readLine();
                    String expireTime = reader.readLine();
                    System.out.println("expireTime: " + expireTime);
                    int expireTimeInt = Integer.parseInt(expireTime);
                    new Thread(() -> {
                        try {
                            Thread.sleep(expireTimeInt);
                            map.remove(key);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
                }
            }
            else if (line.toLowerCase().contains("get")){
                reader.readLine();
                String key = reader.readLine();
                String value = map.get(key);
                if(value == null){
                    clientSocket.getOutputStream().write("$-1\r\n".getBytes());
                }else{
                    clientSocket.getOutputStream().write(("+"+value+"\r\n").getBytes());
                }
            }
            else if (line.toLowerCase().contains("info")){
                // 获取当前端口号
                int port = clientSocket.getPort();
                System.out.println("port: " + port);
                if(role.equals("master")) clientSocket.getOutputStream().write("$11\r\nrole:master\r\n".getBytes());
                else clientSocket.getOutputStream().write("$10\r\nrole:slave\r\n".getBytes());
            }
        }
    }

//    public static void chooseCommand(String input, Socket clientSocket) throws IOException {
////        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));;
//        // 选择命令
////        if (input.contains("PING")) {
////            clientSocket.getOutputStream().write("+PONG\r\n".getBytes());
////        }
//        clientSocket.getOutputStream().write("+PONG\r\n".getBytes());
//    }
}
