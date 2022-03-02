package com.fujitsu.drugsapp.springBatch;

import com.fujitsu.drugsapp.entities.DrugSet;
import org.springframework.batch.item.ItemWriter;

import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

public class UpdateItemWriter implements ItemWriter<DrugSet>, Closeable {
    private final PrintWriter writer;

    public UpdateItemWriter() {
        OutputStream out = null;
        this.writer = new PrintWriter(out);
    }

    @Override
    public void write(final List<? extends DrugSet> items) throws Exception {
        for (DrugSet item : items) {
            writer.println(item.toString());
        }
    }

    @PreDestroy
    @Override
    public void close() throws IOException {
        writer.close();
    }
}
