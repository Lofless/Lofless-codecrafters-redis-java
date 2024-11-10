import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    // System.out.println("Logs from your program will appear here!");

    //  Uncomment this block to pass the first stage
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    int port = 6379;
    try {
      // 创建一个服务端套接字
      serverSocket = new ServerSocket(port);
      // 重复使用本地端口和ip地址
      serverSocket.setReuseAddress(true);
      // accept函数 -> 进入阻塞，等待客户端连接
      clientSocket = serverSocket.accept();

      while(true){
        byte[] input = inputIoStream(clientSocket);
        chooseCommand(input, clientSocket);
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

  // 输入流转换为字符数组
  public static byte[] inputIoStream(Socket clientSocket){
    // 获取客户端输入的字符串
    String input = "";
    // 流输入
    try {
      // 获取数据的通道
      InputStream inputStream = clientSocket.getInputStream();
      byte[] buffer = new byte[1024];
      int bytesRead = inputStream.read(buffer);
      input = new String(buffer, 0, bytesRead);
      return input.getBytes();
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
  }

  public static void chooseCommand(byte[] input, Socket clientSocket){
    // 命令选择
    String command = new String(input);
    // 流输出
    OutputStream outputStream = null;
    try {
      outputStream = clientSocket.getOutputStream();
      outputStream.write("+PONG\r\n".getBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
