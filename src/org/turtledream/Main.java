package org.turtledream;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

class Reader{
    private InputStream inputStream;
    private  int speed;
    private long lastTime = 0;
    Reader(InputStream inputStream, int speed){
        this.inputStream = inputStream;
        this.speed = speed;
    }
    public int read(byte[] b) throws IOException{
        int count =  inputStream.read(b);
        long t = 1000 * count / speed;
        long currentTime = System.currentTimeMillis();
        if( lastTime == 0){
            try {
                Thread.sleep((t));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (currentTime - lastTime < t) {
            try {
                Thread.sleep(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        lastTime = System.currentTimeMillis();
        return count;
    }
}

class Server{
    private static Socket clientSocket;
    private static ServerSocket serverSocket ;
    private static BufferedInputStream bufferedInputStream;
    private static BufferedOutputStream bufferedOutputStream;
    private static byte[] buffer;

    public void run() throws IOException {
        serverSocket = new ServerSocket(6400);
        System.out.println("Сервер запущен.");
        job();
    }
    public void job() throws IOException {
        clientSocket = serverSocket.accept();
        Socket socket = clientSocket;
        new Thread(() -> {
            try {
                System.out.println("Клиент подключен." );
                SendFile(socket);
            }
            catch (Exception ex) {
            }
        }).start();
        new Thread(() -> {
            try {
                job();
            }
            catch (Exception ex) {
            }
        }).start();
    }

    public void SendFile(Socket clientSocket) throws IOException {
        File file = new File("*/Путь к файлу/*");
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedOutputStream = new BufferedOutputStream(clientSocket.getOutputStream());
            buffer = new byte[(int) file.length()];
            int count;

            while ((count = bufferedInputStream.read(buffer)) > 0) {
                bufferedOutputStream.write(buffer, 0, count);
            }

            bufferedOutputStream.close();
            fileInputStream.close();
            bufferedInputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Client{
    private  Socket clientSocket;
    private  BufferedOutputStream bufferedOutputStream;

    public void run(String string) throws IOException, InterruptedException {
        clientSocket = new Socket(string, 6400);
        GetFile();
    }

    public void GetFile() throws IOException {

        try {
            clientSocket.setSoTimeout(5000);
            Random ran = new Random();
            int top = 3;
            char data = ' ';
            String dat = "";

            for (int i=0; i<=top; i++) {
                data = (char)(ran.nextInt(25)+97);
                dat = data + dat;
            }

            File file = new File(dat + ".png");

             InputStream inputStream = clientSocket.getInputStream();
             Reader reader = new Reader(inputStream, 5000);

            byte[] buffer = new byte[clientSocket.getReceiveBufferSize()];
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            this.bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            int count;
            long cur = System.currentTimeMillis();
            while ((count = reader.read(buffer)) >= 0) {
                this.bufferedOutputStream.write(buffer, 0, count);
            }
            long last = System.currentTimeMillis();

            System.out.println("Файл получен. Время: " + (last - cur));

            bufferedOutputStream.close();
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


public class Main{

    public static void main(String[] args) {

        Server server = new Server();
        Client client = new Client();
        Client client2 = new Client();
        Client client3 = new Client();

        new Thread(() -> {
            try {
                server.run();
            }
            catch (Exception ex) {
            }
        }).start();

        new Thread(() -> {
            try {
                client.run("127.0.0.1");
            }
            catch (Exception ex) {
            }
        }).start();
        new Thread(() -> {
            try {
                client2.run("127.0.0.1");
            }
            catch (Exception ex) {
            }
        }).start();
        new Thread(() -> {
            try {
                client3.run("127.0.0.1");
            }
            catch (Exception ex) {
            }
        }).start();
    }
}