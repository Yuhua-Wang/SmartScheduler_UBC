package data;

import InfoNeeded.Section;
import Support.Activity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import static Support.Term.*;

public class JsoupDemo {

    private String myURL;
    private String profURL;
    private String type;
    private String day;
    private String courseTitle;
    private int startingTime;
    private int endingTime;
    private String comments;
    private Elements temp1;
    private Elements temp2;
    Elements result;

    public void dataScraping(String url) {
        myURL = url;
        profURL = splitURL(myURL);
        try {
            Document doc = Jsoup.connect(myURL).get();
            //get course title
            courseTitle = doc.title().split("-")[0];

            // interleaving two sections from online html,
            // after this step all sections of a course would be listed in order
            temp1 = doc.select(".section1");
            temp2 = doc.select(".section2");
            Iterator<Element> l1 = temp1.iterator();
            Iterator<Element> l2 = temp2.iterator();
            result = new Elements();
            while (l1.hasNext() || l2.hasNext()) {
                if (l1.hasNext()) {
                    result.add(l1.next());
                }
                if (l2.hasNext()) {
                    result.add(l2.next());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getURL() {
        return myURL;
    }

    public String getProfURL() {
        return profURL;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    //return a list of sections
    public ArrayList getSections() {
        ArrayList<Section> my_list = new ArrayList<>();
        //format of full year course
        if (result.get(0).child(3).text().length() > 1 || result.get(1).child(1).text().isEmpty()) {
            fullYearCourse(my_list);
        } else {
            termCourse(my_list);
        }
        return my_list;
    }

    public ArrayList fullYearCourse(ArrayList<Section> list) {
        for (int k = 0; k < result.size(); k += 2) {
            Section mySection = null;
            try {
                mySection = setSection(k);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mySection.setTerm(YEAR_TERM);
            String activityType = result.get(k).child(2).text().toUpperCase();
            mySection.setActType(Activity.valueOf(activityType));
            list.add(mySection);
        }
        return list;
    }

    public ArrayList termCourse(ArrayList<Section> list) {
        for (int i = 0; i < result.size(); i++) {
            try {
                Section mySection = setSection(i);
                String curTerm = result.get(i).child(3).text();
                if (Integer.parseInt(curTerm) == 1) {
                    mySection.setTerm(TERM_1);
                } else {
                    mySection.setTerm(TERM_2);
                }
                String activityType = result.get(i).child(2).text().toUpperCase();
                mySection.setActType(Activity.valueOf(activityType));
                System.out.println(mySection.getActType());
                list.add(mySection);
            } catch (Exception e) {
                i--;
            }
        }
        return list;
    }

    public Section setSection(int index) throws Exception {
        String profName;
        Section mySection = new Section();
        String curString = result.get(index).child(1).text();
        mySection.setTitle((curString + " ").split(" ")[2]);
        mySection.setProfURL(profURL);
       // profName = findProf(profURL, mySection.getTitle());
        day = findDay(mySection.getTitle());
      //  mySection.setProf(profName);
        return mySection;
    }

    public String splitURL(String thatURL) {
        String[] split0 = thatURL.split("-");
        String[] split1 = split0[1].split("&");
        return (split0[0] + "-section&" + split1[1] + "&" + split1[2] + "&section=");
    }

    public String findDay(String sectionNum) throws IOException {
        Document dc = Jsoup.connect(profURL + sectionNum).get();
        Elements body = dc.select(".table.table-striped tr");
        String thisDay = body.get(1).child(1).text();
        //System.out.println(thisDay);
        return thisDay;
    }

//    public String findProf(String url, String sectionNum) throws IOException {
//        String name = "";
//        String theURL = url + sectionNum;
//
//        Document dc = Jsoup.connect(theURL).get();
//        Elements body = dc.select("table tr td");
//        try {
//            //ignore the first "numbers" to extract full prof name
//            //for more than 1 prof, currently put their names in one string, need to split for later use
//            Elements b = body.select("a");
//            //if there is no prof element: 1. no room number/prof 2. there is only room number,
//            //then the number is less than 4 char long
//            if (b.size()==0||b.last().text().length()<4) {
//                name = "Ooooops, prof is unavailable";
//            } else {
//                Element a = b.last();
//                String[] splitLastName = a.text().split(",");
//                // when there are more than one ",", there must be more than one prof
//                if (splitLastName.length > 2) {
//                    name = moreThanOneProf();
//                } else {
//                        name = oneProf(splitLastName[0])+oneProf(splitLastName[1]);
//                    }
//            }
////            System.out.println(name);
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//        return name;
//    }
//
//    public String moreThanOneProf() {
//        return "";
//    }
//
//    // when there is a single prof, turn his/her name into the form of last_name+first_name
//    public String oneProf(String name) {
//        String profName = "";
//        String[] splitName = name.split(" ");
//        for (int i = 0; i < splitName.length; i++) {
//            if (i == splitName.length - 1) {
//                profName += splitName[i];
//            } else {
//                profName += splitName[i] + "+";
//            }
//        }
//        return profName;
//    }
}
