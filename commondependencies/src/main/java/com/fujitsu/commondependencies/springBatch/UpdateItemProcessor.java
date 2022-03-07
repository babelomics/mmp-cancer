package com.fujitsu.commondependencies.springBatch;

import com.fujitsu.commondependencies.entities.DrugSet;
import org.springframework.batch.item.ItemProcessor;

public class UpdateItemProcessor implements ItemProcessor<DrugSet, DrugSet> {

    @Override
    public DrugSet process(DrugSet drugSet) throws Exception {
        return null;
    }

}