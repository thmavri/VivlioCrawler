/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thesmartweb.vivliocrawlermaven;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import se.kb.oai.ore.Metadata;
import se.kb.oai.pmh.Header;
import se.kb.oai.ore.AggregatedResource;
import se.kb.oai.pmh.IdentifiersList;
import se.kb.oai.pmh.MetadataFormat;
import se.kb.oai.pmh.MetadataFormatsList;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.oai.pmh.SetsList;
import org.dom4j.tree.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.json.simple.parser.JSONParser;
import se.kb.oai.OAIException;
/**
 *
 * @author themis
 */
public class VivlioCrawlerMavenMain {
    //the names of the following variables are mostly self-explanatory
    public String title;
    public List<String> creators=new ArrayList<String>();//it will include all the students 'names
    public List<String> subjects=new ArrayList<String>();//it captures the subjects of each thesis
    public String description;//abstract of the thesis
    public String datestring;
    public List<String> thesisURLs=new ArrayList<String>();
    public String supervisor;
    public String citation;
    
    protected void VivlioCrawlerMavenMain(){
        this.title="";
        this.description="";
        this.datestring="";
        this.supervisor="";
        this.citation="";
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
     try {
            
            OaiPmhServer server = new OaiPmhServer("http://vivliothmmy.ee.auth.gr/cgi/oai2");
            RecordsList listRecords = server.listRecords("oai_dc");//we capture all the records in oai dc format
            List<VivlioCrawlerMavenMain> listtotal = new ArrayList<VivlioCrawlerMavenMain>();
            //we capture all the names of the professors and former professor of ECE of AUTH from a txt file
            //change the directory to yours
            List<String> profs=Files.readAllLines(Paths.get("/home/themis/NetBeansProjects/VivlioCrawlerMaven/src/main/java/com/thesmartweb/vivliocrawlermaven/profs.txt"));
            
            boolean more=true;//it is a flag used if we encounter more entries than the initial capture
            JSONArray array = new JSONArray();//it is going to be our final total json array
            JSONObject jsonObject = new JSONObject();//it is going to be our final total json object
            while (more) {
                 for (Record rec : listRecords.asList()){
                    VivlioCrawlerMavenMain vc=new VivlioCrawlerMavenMain();
                    Element metadata = rec.getMetadata();
                    if(metadata!=null){
                        //System.out.println(rec.getMetadataAsString());
                        List<Element> elements = metadata.elements();
                        //System.out.println(metadata.getStringValue());
                        for (Element element : elements){
                            String name = element.getName();
                            //we get the title, remove \r, \n and beginning and trailing whitespace
                            if(name.equalsIgnoreCase("title")){
                                vc.title=element.getStringValue();
                                vc.title=vc.title.trim();
                                vc.title=vc.title.replaceAll("(\\r|\\n)", "");
                                if(!(vc.title.endsWith("."))){
                                    vc.title=vc.title+".";//we also add dot in the end for the titles to be uniformed
                                }
                            }
                            if(name.equalsIgnoreCase("creator")){
                                vc.creators.add(element.getStringValue());//we capture the students' names
                            }
                            if(name.equalsIgnoreCase("subject")){
                                vc.subjects.add(element.getStringValue());//we capture the subjects
                            }
                            if(name.equalsIgnoreCase("description")){
                                vc.description=element.getStringValue();//we capture the abstract
                            }
                            if(name.equalsIgnoreCase("date")){
                                vc.datestring=element.getStringValue();
                            }
                            if(name.equalsIgnoreCase("identifier")){
                                if(element.getStringValue().contains("http://")){
                                    vc.thesisURLs.add(element.getStringValue());//we capture the url of the thesis whole file
                                }
                                //if the identifier contains the title then it must be the citation 
                                //out of the citation we need to extract the supevisor's name
                                if(element.getStringValue().contains(vc.title.substring(0,10))){
                                    vc.citation=element.getStringValue();
                                    vc.supervisor=element.getStringValue();
                                    Iterator profsIterator=profs.iterator();
                                    vc.supervisor=vc.supervisor.replace(vc.title,"");//we remove the title out of the citation
                                    //if we have two students we remove the first occurence of "και" which stands for "and"
                                    if(vc.creators.size()==2){
                                         vc.supervisor=vc.supervisor.replaceFirst("και","");
                                    }
                                    //we remove the students' names
                                    Iterator creatorsIterator=vc.creators.iterator();
                                    while(creatorsIterator.hasNext()){
                                        vc.supervisor=vc.supervisor.replace(creatorsIterator.next().toString(),""); 
                                    }
                                    boolean profFlag=false;//flag used that declares that we found the professor that was supervisor
                                    while(profsIterator.hasNext()&&!profFlag){
                                        String prof=profsIterator.next().toString();
                                        //we split the professor's name to surname and name
                                        //because some entries have first the surname and others first the name
                                        String[] profSplitted = prof.split("\\s+");
                                        String supervisorCleared=vc.supervisor;
                                        supervisorCleared=supervisorCleared.replaceAll("\\s+","");//we clear the white space
                                        supervisorCleared=supervisorCleared.replaceAll("(\\r|\\n)", "");//we remove the \r\n
                                        //now we check if the citation includes any name of the professors from the txt
                                        if(supervisorCleared.contains(profSplitted[0])&&supervisorCleared.contains(profSplitted[1])){
                                            vc.supervisor=prof;
                                            profFlag=true;
                                        }
                                    }
                                    //if we don't find the name of the supervisor, we have to perform string manipulation to extract it
                                    if(!profFlag){
                                        vc.supervisor=vc.supervisor.trim();
                                        //we remove the word "Θεσσαλονίκη" which stands for "Thessaloniki" and "Ελλάδα" which stands for Greece
                                        if(vc.supervisor.contains("Θεσσαλονίκη")){
                                            vc.supervisor=vc.supervisor.replaceFirst("Θεσσαλονίκη", "");
                                        }
                                        if(vc.supervisor.contains("θεσσαλονίκη")){
                                            vc.supervisor=vc.supervisor.replaceFirst("θεσσαλονίκη", "");
                                        }
                                        if(vc.supervisor.contains("Ελλάδα")){
                                            vc.supervisor=vc.supervisor.replaceFirst("Ελλάδα", "");
                                        }
                                        if(vc.supervisor.contains("ελλάδα")){
                                            vc.supervisor=vc.supervisor.replaceFirst("ελλάδα", "");
                                        }
                                        //we remove the year and then we should be left only with the supervisor's name
                                        vc.supervisor=vc.supervisor.replace("(","");
                                        vc.supervisor=vc.supervisor.trim();
                                        vc.supervisor=vc.supervisor.replace(")","");
                                        vc.supervisor=vc.supervisor.trim();
                                        vc.supervisor=vc.supervisor.replace(",","");
                                        vc.supervisor=vc.supervisor.trim();
                                        vc.supervisor=vc.supervisor.replace(".","");
                                        vc.supervisor=vc.supervisor.trim();
                                        vc.supervisor=vc.supervisor.replace(vc.datestring.substring(0,4), "");
                                        vc.supervisor=vc.supervisor.trim();
                                    }
                                    //we put everything in a json object
                                    JSONObject obj = new JSONObject();
                                    obj.put("title", vc.title);
                                    obj.put("description", vc.description);
                                    JSONArray creatorsArray = new JSONArray();
                                    creatorsArray.add(vc.creators);
                                    obj.put("creators",creatorsArray);
                                    JSONArray subjectsArray = new JSONArray();
                                    subjectsArray.add(vc.subjects);
                                    obj.put("subjects",subjectsArray);
                                    obj.put("datestring", vc.datestring);
                                    JSONArray thesisURLsArray = new JSONArray();
                                    thesisURLsArray.add(vc.thesisURLs);
                                    obj.put("thesisURLs",thesisURLsArray);
                                    obj.put("supervisor", vc.supervisor); 
                                    obj.put("citation",vc.citation);
                                    //if you are using JSON.simple do this
                                    array.add(obj);    
                                }
                            } 
                        }
                        listtotal.add(vc);//a list containing all the objects
                        //it is not used for now, but created for potential extension of the work
                    }
                }
                 //the following if clause searches for new records
                if (listRecords.getResumptionToken() != null){ 
                    listRecords = server.listRecords(listRecords.getResumptionToken());
                }
                else{ 
                    more = false;
                }
            }
            //we print which records did not have a supervisor
            for(VivlioCrawlerMavenMain vctest:listtotal){
               
                if(vctest.supervisor==null){
                    System.out.println(vctest.title);
                    System.out.println(vctest.citation);
                }
            }
            //we create a pretty json with GSON and we write it into a file
            jsonObject.put("VivliothmmyOldArray" , array);
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(jsonObject.toJSONString()).getAsJsonObject();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJson = gson.toJson(json);
            try {
 
		FileWriter file = new FileWriter("/home/themis/NetBeansProjects/VivlioCrawlerMaven/src/main/java/com/thesmartweb/vivliocrawlermaven/VivliothmmyOldRecords.json");
                file.write(prettyJson);
		file.flush();
		file.close();
 
            } catch (IOException e) {
		System.out.println("Exception: "+e);
            }
 
            //System.out.print(prettyJson);
            
            
            //int j=0;

        } catch (OAIException | IOException e) {
            System.out.println("Exception: "+e);
        }
    }    
}


