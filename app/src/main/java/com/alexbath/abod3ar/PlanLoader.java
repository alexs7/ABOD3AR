package com.alexbath.abod3ar;

import android.content.Context;
import android.content.res.Resources;

import com.recklesscoding.abode.core.plan.Plan;
import com.recklesscoding.abode.core.plan.planelements.drives.DriveCollection;
import com.recklesscoding.abode.core.plan.reader.inst.InstPlanReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.util.List;

class PlanLoader {
    public static void loadPlanFile(String fileName, Context applicationContext) {
        InstPlanReader planReader = new InstPlanReader(applicationContext);
        planReader.readFile(fileName);
        List<DriveCollection> foo = Plan.getInstance().getDriveCollections();
        System.out.println(foo.size());

    }
}
