package com;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileBuildStatusStore {
    private static final String FILE_PATH = "build_statuses.json";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static List<BuildStatus> statuses = new ArrayList<>();
    public static int initialCount = 0;

    static {
        File file = new File(FILE_PATH);
        if (file.exists() && file.length() > 0) {
            try {
                statuses = mapper.readValue(file, new TypeReference<List<BuildStatus>>() {});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            statuses = new ArrayList<>();
        }
        initialCount = statuses.size();

    }

    public synchronized static void addStatus(BuildStatus status) {
        statuses.add(status);
        writeToFile();
    }

    public synchronized static List<BuildStatus> getStatuses() {
        return statuses;
    }

    public synchronized static BuildStatus getStatusById(String id) {
        for (BuildStatus status : statuses) {
            if (status.getId().equals(id)) {
                return status;
            }
        }
        return null;
    }

    private static void writeToFile() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), statuses);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}