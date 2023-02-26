package org.example;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class ClientChat {
    static private int port;
    static private String host;
    static private String login;
    static private final Date date = new Date();
    static private final Scanner console = new Scanner(System.in);
    static private final File fileInfo = new File("settings.txt");
    static private final File fileLog = new File("file.log");

    public static void main(String[] args) {

        createLogInfoFiles();

        try (Socket clientSocket = new Socket(host, port);
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            Thread inMessage = new Thread(() -> inMessageHandler(in, clientSocket));
            Thread outMessage = new Thread(() -> outMessageHandler(out));

            inMessage.start();
            outMessage.start();

            inMessage.join();
            outMessage.join();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void createLogInfoFiles() {
        if (!fileInfo.exists()) {
            try {
                fileInfo.createNewFile();
                fileLog.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileInfo, true))) {
                System.out.println("Введите порт подключения");
                port = Integer.parseInt(console.nextLine());
                System.out.println("Введите имя (host) подключения");
                host = console.nextLine();
                System.out.println("Придумайте и введите логин");
                login = console.nextLine();
                // записываем полученную информацию в файл settings.txt
                bufferedWriter.write(port + "\n");
                bufferedWriter.write(host + "\n");
                bufferedWriter.write(login + "\n");
                writeLog("присоединился к чату");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            // при последующих запусках программы
            // считываем информацию с файла settings.txt
            try (BufferedReader bufferedReaderInfo = new BufferedReader(new FileReader(fileInfo))) {
                port = Integer.parseInt(bufferedReaderInfo.readLine());
                host = bufferedReaderInfo.readLine();
                login = bufferedReaderInfo.readLine();
                writeLog("присоединился к чату");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void outMessageHandler(PrintWriter out) {
        while (true) {
            String message = console.nextLine();
            String dTime = getTime();
            if (message.contains("/exit")) {
                out.println(dTime + ":  " + login + " /exit");
                break;
            }
            String outMessage = dTime + ":  " + login + "  " + message;
            out.println(outMessage);
            writeLog(outMessage + " - this outMessage");
        }
    }

    private static void inMessageHandler(BufferedReader in, Socket clientSocket) {
        String inMessage;
        try {
            while (true) {
                if ((inMessage = in.readLine()) == null) {
                    clientSocket.isClosed();
                    break;
                }
                System.out.println(inMessage);
                writeLog(inMessage);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static String getTime() {
        // формат времени до секунд
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(date);
    }

    private static void writeLog(String message) {
        try (BufferedWriter bufferedWriterLog = new BufferedWriter(new FileWriter(fileLog, true))) {
            bufferedWriterLog.write(message + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}