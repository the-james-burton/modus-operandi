package com.modusoperandi.monitor.dao;

import java.util.Collection;

import com.modusoperandi.model.Process;

public interface ProcessDAO {
    Process getProcess(String windowTitle);

    Collection<Process> getAll();

    Collection<String> getWindowNames();

    void insert(Process process);

    void update(Process process);

    void delete(Process process);

    void deleteAll();

    void insertAll(Collection<Process> processes);
}
