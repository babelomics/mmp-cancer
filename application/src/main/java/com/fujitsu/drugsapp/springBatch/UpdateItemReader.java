package com.fujitsu.drugsapp.springBatch;

import com.fujitsu.drugsapp.entities.DrugSet;
import org.springframework.batch.item.ItemReader;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class UpdateItemReader implements ItemReader<DrugSet> {

    private  String filename;

    private ItemReader<DrugSet> delegate;

    public UpdateItemReader() {}

    @Override
    public DrugSet read() throws Exception {
        DrugSet drugSet;
        return delegate.read();
    }

    private List<DrugSet> drugsets() throws FileNotFoundException {
        List<DrugSet> drugSets = new ArrayList<>();
        return drugSets;
    }
}