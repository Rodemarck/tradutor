/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tradutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.text.StringEscapeUtils;

/**
 *
 * @author roderu
 */
public class Frase {
    public String original;
    public String google;
    public String fim;
    
    public Frase(String original){
        var txts = original.split("<<");
        if(txts.length == 0)
            throw new IllegalArgumentException();
        this.original = txts[0];
        if(txts.length > 1){
            this.google = txts[1];
            if(txts.length> 2)
                this.fim = txts[2];
            else
                this.fim = "";
        }else{
            this.fim = "";
        this.google = "";
        }
        
        /*for(var i=0; i< 20; i++){
            try {
                this.google = translate("ja", "pt", original);
                //System.out.println(google);
                break;
            } catch (IOException ex) {
                Logger.getLogger(Frase.class.getName()).log(Level.SEVERE, null, ex);
                this.google = "";
            }
        }*/
    }
    
    public String asString(){
        return original + "<<" + google + "<<" + fim ;
    }

    public String traduz(){
        for(var i = 0; i< 10; i++){
            try{
                google = translate(original);
                return google;
            }catch(Exception e){}
        }
        return "";
    }

    @Override
    public String toString() {
        return "Frase{" + "original=" + original + ", google=" + google + ", fim=" + fim + '}';
    }

    public String getOriginal() {
        return original;
    }

    public String getGoogle() {
        return google;
    }

    public String getFim() {
        return fim;
    }
    
    
    public Frase(String original, int tentativas){
        
    }
    public static String translate(String langFrom, String langTo, String text) throws IOException {
        // INSERT YOU URL HERE
        String urlStr = "https://script.google.com/macros/s/AKfycbyvoLm349dyHFxdFIMB4Cf1Ns-BvygeSvm7OUvYI14IHg6rPYY/exec" +
                "?q=" + URLEncoder.encode(text, "UTF-8") +
                "&target=" + langTo +
                "&source=" + langFrom;
        URL url = new URL(urlStr);
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
         
        return response.toString();// R.utf8( StringEscapeUtils.unescapeHtml4());
    }
    public static String translate(String txt) throws IOException{
        return translate("ja","pt",txt);
    }
}
