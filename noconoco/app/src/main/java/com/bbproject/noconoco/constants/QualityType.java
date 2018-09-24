package com.bbproject.noconoco.constants;

public class QualityType {

    public static String getQualityNameByKey(String pKey) {
        String name = "?"; //Autre";

        switch (pKey) {
            case "LQ":
                name = "LQ";//Basse";
                break;
            case "HQ":
                name = "MQ";//Moyenne";
                break;
            case "TV":
                name = "TV";//Standard";
                break;
            case "HD_720":
                name = "HD";
                break;
            case "HD_1080":
                name = "FHD";//Full HD";
                break;
        }

        return name;
    }
}
