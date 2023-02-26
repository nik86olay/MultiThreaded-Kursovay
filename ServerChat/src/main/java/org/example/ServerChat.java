package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class ServerChat {
    static int port;
    public static LinkedList<Socket> serverList = new LinkedList<>();
    static private final File fileServerLog = new File("fileServer.log");
    //(System.getProperty("java.class.path")).replace("\\", "/").replaceFirst("/", "//"), "fileServer.log");

    public static void main(String[] args) {

        // считываем номер порта с файла настроек
       readingSettings();

        // создаем ЛогФайл
        createLogFile();

        // стартуем сервер
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Server socket created");

            // стартуем цикл при условии что серверный сокет не закрыт
            while (!server.isClosed()) {

                Socket client = server.accept();
                // добавляем socked в List
                serverList.add(client);

                // отправляем сокет в отдельный поток
                new Thread(() -> ClientHandler(client)).start();
                System.out.println("Connection accepted");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createLogFile() {
        if (!fileServerLog.exists()) {
            try {
                fileServerLog.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void readingSettings() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        try {
            Serv chat = gson.fromJson(new FileReader("serverSettings.json"), Serv.class);
            port = chat.port;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void ClientHandler(Socket client) {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {

            // диалог с клиентом в цикле, пока сокет не закрыт клиентом
            while (!client.isClosed()) {

                // считывание сообщения клиента
                String entry = in.readLine();

                // вывод в консоль сервера и запись в Log-файл
                System.out.println("message on Server - " + entry);
                writeServerLog(entry);

                // проверяем условие работы с клиентом, при выполнении прерываем цикл
                if (entry.contains("/exit")) {

                    break;
                }

                for (Socket i : serverList) {
                    if (!i.equals(client)) {
                        PrintWriter outAll = new PrintWriter(i.getOutputStream(), true);
                        outAll.println(entry);
                    }
                }
            }
            System.out.println("Client disconnected");

            // закрытие сокета клиента и его удаление из списка
            serverList.remove(client);
            in.close();
            client.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeServerLog(String message) {
        try (BufferedWriter bufferedWriterLog = new BufferedWriter(new FileWriter(fileServerLog, true))) {
            bufferedWriterLog.write(message + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
