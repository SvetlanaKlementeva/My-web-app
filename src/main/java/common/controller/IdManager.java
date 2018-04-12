package common.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.model.Constants;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class IdManager {
    public static synchronized int getId(){
        int i = 0;
        try (FileReader reader = new FileReader(Constants.ID_FILE_NAME)) {
             i = new Gson().fromJson(reader, int.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileWriter fileWriter = new FileWriter(Constants.ID_FILE_NAME)) {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            String outputStr = gson.toJson(i+1);
            fileWriter.write(outputStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return i;
    }

}
