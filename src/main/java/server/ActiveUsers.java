package server;

import java.util.ArrayList;
import java.util.List;

//Класс Singleton
public class ActiveUsers {
    List<String> activeUsers;

    //----------------------------------------------------------------------
    private ActiveUsers() {
        activeUsers = new ArrayList<>();
    }

    private static class SingletonHolder {
        private static final ActiveUsers HOLDER_INSTANCE = new ActiveUsers();
    }

    public static ActiveUsers getInstance() {
        return SingletonHolder.HOLDER_INSTANCE;
    }
    //-----------------------------------------------------------------------

    public boolean contains(String login) {
        return activeUsers.contains(login);
    }

    public void add(String login) throws Exception {
        if (contains(login)) {
            throw new Exception("Такой пользователь уже есть");
        }
        activeUsers.add(login);
    }

    public void remove(String login) {
        if (login != null && !login.isEmpty())
            activeUsers.remove(login);
    }
}
