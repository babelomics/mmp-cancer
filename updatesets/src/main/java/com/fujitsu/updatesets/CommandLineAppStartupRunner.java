package com.fujitsu.updatesets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {
    @Autowired
    private UpdateInsertService updateInsertService;

    @Override
    public void run(String...args) throws Exception {
        updateInsertService.checkForUpdate();
    }
}