package com.alexbath.abod3ar;

import android.content.Context;

import com.recklesscoding.abode.core.plan.Plan;
import com.recklesscoding.abode.core.plan.planelements.drives.DriveCollection;
import com.recklesscoding.abode.core.plan.reader.inst.InstPlanReader;

import java.util.List;

class PlanLoader {

    public static List<DriveCollection> loadPlanFile(String fileName, Context applicationContext) {
        InstPlanReader planReader = new InstPlanReader(applicationContext);
        planReader.readFile(fileName);
        List<DriveCollection> driveCollections = Plan.getInstance().getDriveCollections();
        return driveCollections;
    }

}
