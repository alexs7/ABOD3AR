package com.alexbath.abod3ar;

import android.content.Context;

import com.recklesscoding.abode.core.plan.Plan;
import com.recklesscoding.abode.core.plan.planelements.drives.DriveCollection;
import com.recklesscoding.abode.core.plan.reader.inst.InstPlanReader;

import java.util.List;

class PlanLoader {

    public static List<DriveCollection> loadPlanFile(String fileName, Context applicationContext) {
        Plan.getInstance().cleanAllLists();
        InstPlanReader planReader = new InstPlanReader(applicationContext);
        planReader.readFile(fileName);
        return Plan.getInstance().getDriveCollections();
    }

}
