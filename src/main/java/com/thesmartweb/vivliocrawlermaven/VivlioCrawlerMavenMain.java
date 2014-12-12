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
    public String title;
    public List<String> creators=new ArrayList<String>();
    public List<String> subjects=new ArrayList<String>();
    public String description;
    public String datestring;
    public String thesisURL;
    public String supervisor;
    public String citation;
    
    protected void VivlioCrawlerMavenMain(){
        this.title="";
        this.description="";
        this.datestring="";
        this.thesisURL="";
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
            RecordsList listRecords = server.listRecords("oai_dc");
            List<VivlioCrawlerMavenMain> listtotal = new ArrayList<VivlioCrawlerMavenMain>();
            List<String> profs=Files.readAllLines(Paths.get("/home/themis/NetBeansProjects/VivlioCrawler/src/vivliocrawler/profs.txt"));
            
            boolean more=true;
            JSONArray array = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            while (more) {
                 for (Record rec : listRecords.asList()){
                    VivlioCrawlerMavenMain vc=new VivlioCrawlerMavenMain();
                    List<String> creatorsLocal=new ArrayList<String>();
                    List<String> subjectsLocal=new ArrayList<String>();
                    Element metadata = rec.getMetadata();
                    if(metadata!=null){
                        //System.out.println(rec.getMetadataAsString());
                        List<Element> elements = metadata.elements();
                        //System.out.println(metadata.getStringValue());
                        for (Element element : elements){
                            String name = element.getName();
                            if(name.equalsIgnoreCase("title")){
                                vc.title=element.getStringValue();
                                vc.title=vc.title.trim();
                                vc.title=vc.title.replaceAll("(\\r|\\n)", "");
                                if(!(vc.title.endsWith("."))){
                                    vc.title=vc.title+".";
                                }
                            }
                            if(name.equalsIgnoreCase("creator")){
                                vc.creators.add(element.getStringValue());
                            }
                            if(name.equalsIgnoreCase("subject")){
                                vc.subjects.add(element.getStringValue());
                            }
                            if(name.equalsIgnoreCase("description")){
                                vc.description=element.getStringValue();
                            }
                            if(name.equalsIgnoreCase("date")){
                                vc.datestring=element.getStringValue();
                            }
                            if(name.equalsIgnoreCase("identifier")){
                                if(element.getStringValue().contains("http://")&&element.getStringValue().contains(".pdf")){
                                    vc.thesisURL=element.getStringValue();
                                }
                                
                                if(element.getStringValue().contains(vc.title.substring(0,10))){
                                    vc.citation=element.getStringValue();
                                    vc.supervisor=element.getStringValue();
                                    Iterator profsIterator=profs.iterator();
                                    vc.supervisor=vc.supervisor.replace(vc.title,"");
                                    if(vc.creators.size()==2){
                                         vc.supervisor=vc.supervisor.replaceFirst("και","");
                                    }
                                    Iterator creatorsIterator=vc.creators.iterator();
                                    while(creatorsIterator.hasNext()){
                                        vc.supervisor=vc.supervisor.replace(creatorsIterator.next().toString(),""); 
                                    }
                                    boolean profFlag=false;
                                    while(profsIterator.hasNext()&&!profFlag){
                                        String prof=profsIterator.next().toString();
                                        String[] profSplitted = prof.split("\\s+");
                                        String supervisorCleared=vc.supervisor;
                                        supervisorCleared=supervisorCleared.replaceAll("\\s+","");
                                        supervisorCleared=supervisorCleared.replaceAll("(\\r|\\n)", "");
                                        if(supervisorCleared.contains(profSplitted[0])&&supervisorCleared.contains(profSplitted[1])){
                                            vc.supervisor=prof;
                                            profFlag=true;
                                        }
                                        
                                        
                                    }
                                    if(!profFlag){
                                        vc.supervisor=vc.supervisor.trim();
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
                                    obj.put("thesisURL", vc.thesisURL);
                                    obj.put("supervisor", vc.supervisor); 
                                    obj.put("citation",vc.citation);
                                    //if you are using JSON.simple do this
                                    array.add(obj);                                
                                    int b=0;
                                }
                            }                        
                            //System.out.println(element.getStringValue());
                        }
                        listtotal.add(vc);
                        int k=0;
                    }
                }
                if (listRecords.getResumptionToken() != null){ 
                    listRecords = server.listRecords(listRecords.getResumptionToken());
                }
                else{ 
                    more = false;
                }
            }
            
            for(VivlioCrawlerMavenMain vctest:listtotal){
               
                if(vctest.supervisor==null){
                    System.out.println(vctest.title);
                    System.out.println(vctest.citation);
                }
            }
            
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


