package server;

import common.model.Constants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class StartServer {

    //Запуск сервера осуществляется тут
    //Создаёт экземпляры серверов для клиентов(многопоточность)
    public static void main(String[] args) {
        System.out.println("ServerStart: Start");
        try (ServerSocket server = new ServerSocket(Constants.SERVER_PORT);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("ServerStart: Серверный сокет создан.");
            System.out.println("ServerStart: Входим в цикл ожидания запроса(выход - quit)...");

            // Создаем пул для потоков сервера
            // Пул при необходимости создает новые потоки,
            // но будет использовать ранее созданные потоки, когда они будут доступны
            ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

            server.setSoTimeout(1000);

            while (!server.isClosed()) {

                if (consoleInput.ready()) {
                    System.out.println("ServerStart: Получил сообщение из консоли. Посмотрим что в нём...");

                    if (consoleInput.readLine().equalsIgnoreCase("quit")) {
                        System.out.println("ServerStart: Получили команду на завершение работы...");
                        server.close();
                        threadPool.shutdown();
                        break;
                    }
                }

                try {
                    // Отправляем поток в пул
                    threadPool.submit(new Thread(new Server(server.accept())));
                    System.out.println("ServerStart: Создали новый сервер...");
                }
                catch (SocketTimeoutException ex){
                }
            }
            System.out.println("ServerStart: Сервер завершил свою работу...");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("ServerStart: FIN!");
        }
    }
}
