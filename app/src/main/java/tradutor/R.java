/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tradutor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

/**
 *
 * @author roderu
 */
public class R {
    public static String utf8(String str){
        return new String(str.getBytes(),StandardCharsets.UTF_8);
    }
    public static LinkedList<String> ler(String path) throws IOException{
        var f = new File(path);//"C:\\Users\\roderu\\Desktop\\2weeks.txt"
        System.out.println("lendo :" + f.getAbsolutePath());
        var txts = new LinkedList<String>();
        var aux = "";
        try(var fr = new FileReader(f);
            BufferedReader bf = new BufferedReader(fr)){
            while((aux = bf.readLine()) != null)
                txts.add(utf8(aux));
        }
        return txts;
    }
    public static LinkedList<String> recupera(){
        return null;
    }
    static void salvar(String path, String txt) throws IOException {
        var f = new File(path);//"C:\\Users\\roderu\\Desktop\\2weeks.txt"
        var txts = new LinkedList<String>();
        var aux = "";
        try(var fr = new FileWriter(f);
            var bf = new BufferedWriter(fr)){
            bf.write(txt);
        }
    }
    static void persistir(String path, String pathTemp){
        var f = new File(path);
        var fp = new File(pathTemp);
        //if(fp.exists())
    }
}
