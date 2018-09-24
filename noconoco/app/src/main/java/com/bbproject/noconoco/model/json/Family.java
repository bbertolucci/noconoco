package com.bbproject.noconoco.model.json;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.bbproject.noconoco.model.ShowArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class Family implements Parcelable, Serializable {

    public static final Parcelable.Creator<Family> CREATOR = new Parcelable.Creator<Family>() {
        public Family createFromParcel(Parcel in) {
            return new Family(in);
        }

        public Family[] newArray(int size) {
            return new Family[size];
        }
    };
    private static final long serialVersionUID = 5226797767157513996L;
    private static final String TAG = "Family";
    private ShowArrayList mList = new ShowArrayList();
    private int mCurrentPage = 0;
    private String mGeoloc;
    private String mfamilyTT;
    private String mIdPartner;
    private Integer mIdFamily;
    private String mPartnerShortname;
    private String mPartnerKey;
    private String mScreenshot128;
    private String mScreenshot256;
    private String mScreenshot512;
    private String mFamilyResume;
    private String mFamilyKey;
    private Integer mNbShows;

    private Family() {
        super();
    }

    private Family(Parcel in) {
        super();
        readFromParcel(in);
    }

    public static void decodeJSON(String pResponse, ArrayList<Family> pList) throws JSONException {
        JSONArray root_object = new JSONArray(pResponse);
        int count = root_object.length();
        for (int i = 0; i < count; i++) {
            try {
                Family family = new Family();
                JSONObject first = root_object.getJSONObject(i);
                family.mIdFamily = first.optInt("id_family", 0);
                family.mfamilyTT = first.optString("family_TT", "");
                family.mGeoloc = first.optString("mGeoloc", "fr");
                family.mPartnerShortname = first.optString("partner_shortname", "Nolife");
                family.mPartnerKey = first.optString("partner_key", "NOL");
                family.mNbShows = first.optInt("nb_shows", 0);
                family.mFamilyKey = first.optString("family_key", "");
                family.mScreenshot128 = first.optString("icon_128x72", "");
                family.mScreenshot256 = first.optString("icon_256x144", "");
                family.mScreenshot512 = first.optString("icon_512x288", "");
                family.mFamilyResume = first.optString("family_resume", "");
                pList.add(family);
            } catch (JSONException e) {
                Log.e(TAG, "JSONException", e);
            }
        }
    }

    public static int[] getColorsByFamilyId(int pId) { //Those data were not sent in the api, so I hardcoded it. Server api should be modify
        int[] colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};

        switch (pId) {
            case 214: //id_family: 214,	family_TT: "(Vous savez) Pourquoi on est là"
                colors = new int[]{0xAA936e8f, 0xAA936e8f, 0x00FF0000};
                break;
            case 3: //id_family: 3,	family_TT: "101%"
                colors = new int[]{0x77FF0000, 0x77FF0000, 0x00FF0000};
                break;
            case 91: //id_family: 91,	family_TT: "1D6"
                colors = new int[]{0xBB000000, 0xBB000000, 0x00FF0000};
                break;
            case 68: //id_family: 68,	family_TT: "3mn pas + pour parler d'un jeu 3+"
                colors = new int[]{0x77ffb8e4, 0x77ffb8e4, 0x00FF0000};
                break;
            case 139: //id_family: 139,	family_TT: "56Kast"
                colors = new int[]{0x77b9ef29, 0x774f790b, 0x00FF0000};
                break;
            case 211: //id_family: 211,	family_TT: "À lire, à voir"
                colors = new int[]{0xAA936e8f, 0xAA936e8f, 0x00FF0000};
                break;
            case 142: //id_family: 142,	family_TT: "À Table !"
                colors = new int[]{0x77FFFFFF, 0xAA000000, 0x00FF0000};
                break;
            case 257: //id_family: 257,	family_TT: "Annonces"
                colors = new int[]{0xAA000000, 0xAA000000, 0x00FF0000};
                break;
            case 117: //id_family: 117,	family_TT: "Another Hero"
                colors = new int[]{0x77FF0000, 0x77FF0000, 0x00FF0000};
                break;
            case 21: //id_family: 21,	family_TT: "BD Blogueurs"
                colors = new int[]{0xAA000000, 0x77FFFFFF, 0x00FF0000};
                break;
            case 105: //id_family: 105,	family_TT: "Big Bug Hunter"
                colors = new int[]{0x7700ace7, 0x774ff4f0, 0x00FF0000};
                break;
		/*	case 191 : //id_family: 191,	family_TT: "BLAZBLUE ALTER MEMORY"
				colors = new int[] {0xAA000000,0xAA000000,0x00FF0000};
				break;
			case 172 : //id_family: 172,	family_TT: "C-Control"
				colors = new int[] {0xAA000000,0xAA000000,0x00FF0000};
				break;
			case 242 : //id_family: 242,	family_TT: "Casshern Sins"
				colors = new int[] {0xAA000000,0xAA000000,0x00FF0000};
				break;*/
            case 78: //id_family: 78,	family_TT: "Catsuka"
                colors = new int[]{0x77e04c1c, 0x77ffffff, 0x00FF0000};
                break;
            case 92: //id_family: 92,	family_TT: "Ce soir j'ai raid"
                colors = new int[]{0x775780a0, 0x775780a0, 0x00FF0000};
                break;
            case 69: //id_family: 69,	family_TT: "Chaud Time"
                colors = new int[]{0x77FF0000, 0x77ffb8e4, 0x00FF0000};
                break;
            case 22: //id_family: 22,	family_TT: "Chez Marcus"
                colors = new int[]{0x7700ace7, 0x77FFFFFF, 0x00FF0000};
                break;
            case 12: //id_family: 12,	family_TT: "Classés 18+"
                colors = new int[]{0xAA000000, 0x77FF0000, 0x00FF0000};
                break;
            case 77: //id_family: 77,	family_TT: "Compiler"
                colors = new int[]{0x77FFFFFF, 0x77fbf483, 0x00FF0000};
                break;
            case 76: //id_family: 76,	family_TT: "Costume Player"
                colors = new int[]{0x7700ace7, 0x77ffb8e4, 0x00FF0000};
                break;
            case 120: //id_family: 120,	family_TT: "Côté Comics"
                colors = new int[]{0x77FF0000, 0x77fff43e, 0x00FF0000};
                break;
            case 102: //id_family: 102,	family_TT: "CréAtioN"
                colors = new int[]{0xAA000000, 0x77936e8f, 0x00FF0000};
                break;
            case 8: //id_family: 8,	family_TT: "Critique"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 220: //id_family: 220,	family_TT: "Crunch Time"
                colors = new int[]{0x7700ace7, 0x77789196, 0x00FF0000};
                break;
			/*case 245 : //id_family: 245,	family_TT: "Darker than Black"
				colors = new int[] {0xAA000000,0xAA000000,0x00FF0000};
				break;*/
            case 28: //id_family: 28,	family_TT: "Debug Mode"
                colors = new int[]{0x778db61f, 0x778db61f, 0x00FF0000};
                break;
            case 29: //id_family: 29,	family_TT: "Deux minutes pour parler de..."
                colors = new int[]{0xAA000000, 0x77FFFFFF, 0x00FF0000};
                break;
            case 138: //id_family: 138,	family_TT: "Devil'Slayer"
                colors = new int[]{0xAA000000, 0x77936e8f, 0x00FF0000};
                break;
            case 61: //id_family: 61,	family_TT: "Documentaire"
                colors = new int[]{0x77FF0000, 0x77FFFFFF, 0x00FF0000};
                break;
            case 63: //id_family: 63,	family_TT: "Double Face"
                colors = new int[]{0xAA000000, 0x77FFFFFF, 0x00FF0000};
                break;
            case 86: //id_family: 86,	family_TT: "écrans.fr, le podcast"
                colors = new int[]{0x77ffffff, 0x77ffffff, 0x00FF0000};
                break;
            case 223: //id_family: 223,	family_TT: "Esprit Japon"
                colors = new int[]{0x77FF0000, 0x77FFFFFF, 0x00FF0000};
                break;
            case 30: //id_family: 30,	family_TT: "EXP"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 93: //id_family: 93,	family_TT: "Extra Life"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 248: //id_family: 248,	family_TT: "Film"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 46: //id_family: 46,	family_TT: "Film amateur"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 197: //id_family: 197,	family_TT: "Cool Guys, Hot Ramen"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 31: //id_family: 31,	family_TT: "Format Court"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 184: //id_family: 184,	family_TT: "Fractale"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 137: //id_family: 137,	family_TT: "France Five"
                colors = new int[]{0x77f6ff00, 0xAA000000, 0x77FF0000, 0x770000FF, 0x77ff78f6, 0x00FF0000};
                break;
            case 88: //id_family: 88,	family_TT: "Game Center"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 33: //id_family: 33,	family_TT: "Geek's Life"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
/*			case 254 : //id_family: 254,	family_TT: "Gundam Reconguista in G"
				colors = new int[] {0xAA000000,0xAA000000,0x00FF0000};
				break;*/
            case 35: //id_family: 35,	family_TT: "Hall of Shame"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 107: //id_family: 107,	family_TT: "Hard Corner"
                colors = new int[]{0xBBff0000, 0xBB000000, 0xBBff0000, 0x00FF0000};
                break;
            case 37: //id_family: 37,	family_TT: "Hidden Palace"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
/*			case 140 : //id_family: 140,	family_TT: "Hôkago Midnighters"
				colors = new int[] {0xAA000000,0xAA000000,0x00FF0000};
				break;*/
            case 206: //id_family: 206,	family_TT: "I Need Romance"
                colors = new int[]{0x77FFFFFF, 0x77ffb8e4, 0x00FF0000};
                break;
            case 66: //id_family: 66,	family_TT: "J-TOP (Speed run)"
                colors = new int[]{0x77FF0000, 0x77FF0000, 0x00FF0000};
                break;
            case 103: //id_family: 103,	family_TT: "J'ai jamais su dire non"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 65: //id_family: 65,	family_TT: "Jamais sans 1%"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 75: //id_family: 75,	family_TT: "Japan in Motion"
                colors = new int[]{0x77FFFFFF, 0x77FF0000, 0x77FFFFFF, 0x00FF0000};
                break;
            case 133: //id_family: 131,	family_TT: "Jeu-Top"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 175: //id_family: 175,	family_TT: "Que sa volonté soit faite"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 143: //id_family: 143,	family_TT: "Kyô no wanko - Le Toutou du jour"
                colors = new int[]{0x77FFFFFF, 0x77ffb8e4, 0x00FF0000};
                break;
            case 10: //id_family: 10,	family_TT: "L'Instant Kamikaze"
                colors = new int[]{0xAA000000, 0xAA000000, 0x00FF0000};
                break;
            case 144: //id_family: 144,	family_TT: "La Grosse Partie"
                colors = new int[]{0xAA000000, 0x77fff410, 0x00FF0000};
                break;
            case 14: //id_family: 14,	family_TT: "La minute du geek"
                colors = new int[]{0xAA000000, 0x77FFFFFF, 0x00FF0000};
                break;
            case 154: //id_family: 154,	family_TT: "Le Blog de Gaea"
                colors = new int[]{0x77ff10da, 0x77ff10da, 0x00FF0000};
                break;
            case 67: //id_family: 67,	family_TT: "Le coin des abonnés"
                colors = new int[]{0x77FF0000, 0x77FF0000, 0x00FF0000};
                break;
            case 39: //id_family: 39,	family_TT: "Le jeu du ***"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 235: //id_family: 235,	family_TT: "Le Point Info"
                colors = new int[]{0x77FF0000, 0x77FF0000, 0x00FF0000};
                break;
            case 94: //id_family: 94,	family_TT: "Le point sur Nolife"
                colors = new int[]{0x77FF0000, 0x77FF0000, 0x00FF0000};
                break;
            case 80: //id_family: 80,	family_TT: "Le Visiteur du Futur"
                colors = new int[]{0xAA000000, 0x7705e700, 0x00FF0000};
                break;
            case 116: //id_family: 116,	family_TT: "Les Blablagues de Laurent-Laurent"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 111: //id_family: 111,	family_TT: "Les Oubliés de la Playhistoire"
                colors = new int[]{0xAAfbffcd, 0xAAfbffcd, 0x00FF0000};
                break;
            case 115: //id_family: 115,	family_TT: "Les vacances de Nolife"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 194: //id_family: 194,	family_TT: "Live report"
                colors = new int[]{0x77FF0000, 0x77FFFFFF, 0x00FF0000};
                break;
            case 43: //id_family: 43,	family_TT: "Mange mon geek"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 200: //id_family: 200,	family_TT: "Lucifer"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 251: //id_family: 251,	family_TT: "Metal Hurlant Chronicles"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 127: //id_family: 127,	family_TT: "Metal Missile & Plastic Gun"
                colors = new int[]{0x7700ace7, 0x77FFFFFF, 0x00FF0000};
                break;
            case 79: //id_family: 79,	family_TT: "Mon Nolife à Moi"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 44: //id_family: 44,	family_TT: "Mon souvenir"
                colors = new int[]{0xAA000000, 0x775d741e, 0x00FF0000};
                break;
			/*case 239 : //id_family: 239,	family_TT: "Mondaiji"
				colors = new int[] {0xAA000000,0xAA000000,0x00FF0000};
				break;*/
            case 81: //id_family: 81,	family_TT: "Money Shot"
                colors = new int[]{0xAA000000, 0x7700ace7, 0x00FF0000};
                break;
            case 260: //id_family: 260,	family_TT: "Journal de Monster Hunter : Le Village Felyne au bord du Gouffre G"
                colors = new int[]{0x7700ace7, 0x77936e8f, 0x00FF0000};
                break;
            case 203: //id_family: 203,	family_TT: "Playful Kiss"
                colors = new int[]{0x77FFFFFF, 0x77ffb8e4, 0x00FF0000};
                break;
            case 109: //id_family: 109,	family_TT: "Nihongo ga dekimasu ka"
                colors = new int[]{0x77FFFFFF, 0x77FF0000, 0x77FFFFFF, 0x00FF0000};
                break;
            case 82: //id_family: 82,	family_TT: "Nochan"
                colors = new int[]{0xAA000000, 0x7700ef29, 0x00FF0000};
                break;
/*			case 232 : //id_family: 232,	family_TT: "Noir"
				colors = new int[] {0xAA000000,0xAA000000,0x00FF0000};
				break;*/
            case 62: //id_family: 62,	family_TT: "Nolife"
                colors = new int[]{0x77FF0000, 0x77FFFFFF, 0x00FF0000};
                break;
            case 95: //id_family: 95,	family_TT: "Nolife Emploi IRL"
                colors = new int[]{0xAA000000, 0x77b9ef29, 0x00FF0000};
                break;
            case 148: //id_family: 148,	family_TT: "Noob"
                colors = new int[]{0xAA000000, 0x77936e8f, 0x00FF0000};
                break;
            case 19: //id_family: 19,	family_TT: "One-shot"
                colors = new int[]{0x77936e8f, 0xAA000000, 0x00FF0000};
                break;
            case 47: //id_family: 47,	family_TT: "Oscillations"
                colors = new int[]{0x7700ace7, 0x77b9ef29, 0x00FF0000};
                break;
            case 48: //id_family: 48,	family_TT: "OTO"
                colors = new int[]{0xAA000000, 0x77FF0000, 0x77FF0000, 0x00FF0000};
                break;
            case 6: //id_family: 6,	family_TT: "OTO EX"
                colors = new int[]{0xAA000000, 0x77FF0000, 0x77FF0000, 0x00FF0000};
                break;
            case 4: //id_family: 4,	family_TT: "OTO Play"
                colors = new int[]{0xAA000000, 0x77FF0000, 0x77FF0000, 0x00FF0000};
                break;
            case 98: //id_family: 98,	family_TT: "PICO PICO"
                colors = new int[]{0x7700ace7, 0x77ffffff, 0x00FF0000};
                break;
            case 51: //id_family: 51,	family_TT: "PIXA"
                colors = new int[]{0x77fffc00, 0xAA000000, 0x00FF0000};
                break;
            case 145: //id_family: 145,	family_TT: "Purgatoire"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 9: //id_family: 9,	family_TT: "Reportage"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 54: //id_family: 54,	family_TT: "Retro & Magic"
                colors = new int[]{0x77fffc00, 0x7700ace7, 0x77fffc00, 0x00FF0000};
                break;
            case 100: //id_family: 100,	family_TT: "Rêves et Cris"
                colors = new int[]{0x77fff2ad, 0xAA000000, 0x77fff2ad, 0x00FF0000};
                break;
            case 55: //id_family: 55,	family_TT: "Roadstrip"
                colors = new int[]{0x77FFFFFF, 0x77FF0000, 0x00FF0000};
                break;
			/*case 229 : //id_family: 229,	family_TT: "Robotics;Notes"
				colors = new int[] {0xAA000000,0xAA000000,0x00FF0000};
				break;*/
            case 96: //id_family: 96,	family_TT: "Skill"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 99: //id_family: 99,	family_TT: "Smartphones & Tablettes"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 60: //id_family: 60,	family_TT: "Soirée spéciale"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 5: //id_family: 5,	family_TT: "Superplay"
                colors = new int[]{0xAA000000, 0x7714a701, 0x00FF0000};
                break;
            case 126: //id_family: 126,	family_TT: "Technologie de l’Information en Pratique et Sans danger"
                colors = new int[]{0x77fff000, 0x77ff9600, 0x00FF0000};
                break;
            case 57: //id_family: 57,	family_TT: "Temps Perdu"
                colors = new int[]{0x77ff9600, 0x77ffb8e4, 0x00FF0000};
                break;
            case 7: //id_family: 7,	family_TT: "Temps Réel"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 166: //id_family: 166,	family_TT: "The Guild"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 108: //id_family: 108,	family_TT: "The Place to Be"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 188: //id_family: 188,	family_TT: "Thermæ Romæ"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 97: //id_family: 97,	family_TT: "toco toco"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 56: //id_family: 56,	family_TT: "Tôkyô Café"
                colors = new int[]{0x77ff009c, 0x77FFFFFF, 0x00FF0000};
                break;
			/*case 169 : //id_family: 169,	family_TT: "Tsuritama"
				colors = new int[] {0xAA000000,0xAA000000,0x00FF0000};
				break;
			case 181 : //id_family: 181,	family_TT: "Un drôle de père"
				colors = new int[] {0xAA000000,0xAA000000,0x00FF0000};
				break;*/
            case 110: //id_family: 110,	family_TT: "Very Hard"
                colors = new int[]{0xAA000000, 0xAA000000, 0x00FF0000};
                break;
            case 104: //id_family: 104,	family_TT: "War Pigs"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 151: //id_family: 151,	family_TT: "WarpZone Project"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 130: //id_family: 130,	family_TT: "WiP – Work in Progress"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
            case 178: //id_family: 178,	family_TT: "Wizard Barristers"
                colors = new int[]{0x7700ace7, 0x7700ace7, 0x00FF0000};
                break;
        }
        return colors;
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public void setCurrentPage(int pCurrentPage) {
        this.mCurrentPage = pCurrentPage;
    }

    public ShowArrayList getList() {
        return mList;
    }

    public void setList(ShowArrayList pList) {
        this.mList = pList;
    }

    public Integer getIdFamily() {
        return mIdFamily;
    }

    public String getScreenshot128() {
        return mScreenshot128;
    }

    public String getScreenshot256() {
        return mScreenshot256;
    }

    public String getScreenshot512() {
        return mScreenshot512;
    }

    public String getGeoloc() {
        return mGeoloc;
    }

    public Integer getNbShows() {
        return mNbShows;
    }

    public String getFamilyTT() {
        return mfamilyTT;
    }

    public String getFamilyKey() {
        return mFamilyKey;
    }

    public String getPartnerShortname() {
        return mPartnerShortname;
    }

    public String getPartnerKey() {
        return mPartnerKey;
    }

    public String getFamilyResume() {
        return mFamilyResume;
    }

    private void readFromParcel(Parcel in) {

        mList = (ShowArrayList) in.readSerializable();
        mCurrentPage = in.readInt();
        mGeoloc = in.readString();
        mfamilyTT = in.readString();
        mIdPartner = in.readString();
        mIdFamily = in.readInt();
        mPartnerShortname = in.readString();
        mPartnerKey = in.readString();
        mScreenshot128 = in.readString();
        mScreenshot256 = in.readString();
        mScreenshot512 = in.readString();
        mFamilyResume = in.readString();
        mFamilyKey = in.readString();
        mNbShows = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mList);
        dest.writeInt(mCurrentPage);
        dest.writeString(mGeoloc);
        dest.writeString(mfamilyTT);
        dest.writeString(mIdPartner);
        dest.writeInt(mIdFamily);
        dest.writeString(mPartnerShortname);
        dest.writeString(mPartnerKey);
        dest.writeString(mScreenshot128);
        dest.writeString(mScreenshot256);
        dest.writeString(mScreenshot512);
        dest.writeString(mFamilyResume);
        dest.writeString(mFamilyKey);
        dest.writeInt(mNbShows);
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) return true;
        if (!(that instanceof Family)) return false;
        Family thatFamily = (Family) that;
        return null != thatFamily.getIdFamily() && thatFamily.getIdFamily().intValue() == this.getIdFamily().intValue();
    }

}
