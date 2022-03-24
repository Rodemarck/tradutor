/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package tradutor;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author roderu
 */
public class Abridor extends javax.swing.JFrame {
    private LinkedList<String> arqs;
    private List<Frase> frases;
    private Frase fraseAtual;
    private int len;
    private static Clipboard clipboard;
    private Thread[] pool;
    private String logFile;
    private String file;
    private Texto texto;
    /**
     * Creates new form Abridor
     */
    public Abridor() {
        initComponents();
        texto = new Texto();
        var filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
        LeitorArquivos.setFileFilter(filter);
        LeitorArquivos.addChoosableFileFilter(filter);
        logFile = "logs\\arquivo_temp_" + System.currentTimeMillis() +".txt";
        file = "C:\\Users\\roderu\\Desktop\\memoria.txt";
        lbArq.setText("arquivo.txt");
        pool = new Thread[10];
        LeitorArquivos.setDialogTitle("arquivos");
       setIconImage(new ImageIcon(getClass().getResource("/ico/circle.png")).getImage());
        arqs = new LinkedList<>();
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        
        taGa.setEditable(true);
        taJp.setEditable(false);
        taFim.setEditable(true);
        taJp.setLineWrap(true);
        taGa.setLineWrap(true);
        taFim.setLineWrap(true);
        setTitle("Tradutor");
        lista.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (renderer instanceof JLabel && value instanceof Frase) {
                    ((JLabel) renderer).setText(index + ":" +((Frase) value).original);
                }
                return renderer;
            }
        });
        //lista.setBorder(BorderFactory.createEmptyBorder(0,0, 0, 30));
        iniciaLista(file);
        /*addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(WindowEvent winEvt) {
                LeitorArquivos.showDialog(null,null);
            }
        });*/
    }

    
  
    private void atualiza(){
        atualiza( lista.getSelectedIndex());

    }
    private void atualiza(int index){
        var frase = frases.get(index);
        var g = taGa.getText().replace("\n", "") .replace("\\s+"," ");
        if(g.length() > 5)
            frase.google = g;
        g = taFim.getText().replace("\n", "");
        if(g.length() > 5)
            frase.fim = g;
        //taFim.setText("");
        //taGa.setText("");
    }
    
    private void scroll(int index){
        lista.scrollRectToVisible(new Rectangle(lista.getCellBounds(index, index)));
    }
    public static void copiar(String s){
        clipboard.setContents(new StringSelection(s), null);
    }
    private void mostra(){
        var index = lista.getSelectedIndex();
        lbNumero.setText(String.valueOf(index));
        lbPorcent.setText(String.valueOf(((double)(index+1)*100)/len));
//        LeitorArquivos.setDialogTitle("arquivos");
        fraseAtual = frases.get(index);
        
        taJp.setText(fraseAtual.original);
        taGa.setText(fraseAtual.google);
        taFim.setText(fraseAtual.fim);
        
        scroll(index);
        if(!btnPiloto.isSelected())
            taFim.requestFocus();
        if(btnAutoSave.isSelected())
            salvar();
        if(btnAutoMini.isSelected())
            setState(ICONIFIED);
        if(btnAutoC.isSelected())
            copiar(fraseAtual.original);
    }
    
    private List<Frase> constroiFrases(LinkedList<String> strs) throws InterruptedException{
        var frases = new LinkedList<Frase>();
        
        for(var str:strs){
            try{
                frases.add(new Frase(str));
            }catch(Exception e){
                
            }
        }
        return frases;
    }
    private int getPrimeiro(){
        var in = 0;
            for(var f:frases){
                if(f.fim.length() == 0){
                    return in;
                }
                ++in;
            }
        return -1;
    }
    private void iniciaLista(String arq){
        try {
            var l =  R.ler(arq);
            len = l.size();
            lbCount.setText(String.valueOf(len));
            var demoList = new DefaultListModel();
            frases = constroiFrases(l);
            frases.forEach(f ->{
                if(f.google.contains("html"))
                    f.google = "";
            });
            frases.forEach(demoList::addElement);
            lista.setModel(demoList);
            lista.setSelectedIndex(0);
            
            var in = getPrimeiro();
            lista.setSelectedIndex(in);
            scroll(in);
            mostra();
            
        } catch (Exception ex) {
            Logger.getLogger(Abridor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void aoDesligar(){
    }
    public void salvar(){
        var txt = frases.stream()
                .map(Frase::asString)
                .collect(Collectors.joining("\n"));
        try {
            R.salvar(file,txt);
        } catch (IOException ex) {
            Logger.getLogger(Abridor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private synchronized void muda(int n){
        var i = lista.getSelectedIndex()+n;
        if(i < 0 || i >= len)
            return;
        lista.setSelectedIndex(i);
        mostra();
    }
    private synchronized void proximo(){
        muda(1);
    }
    private synchronized void anterior(){
        muda(-1);
    }
    private boolean navegar(int key){
        if(btnNavegar.isSelected()) {
            atualiza();
            muda(switch (key) {
                case 37 -> -1;
                case 38 -> -10;
                case 39 -> 1;
                case 40 -> 10;
                default -> 0;
            });
        }
        return false;
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        LeitorArquivos = new javax.swing.JFileChooser();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        btnAutoSave = new javax.swing.JCheckBox();
        btnAutoMini = new javax.swing.JCheckBox();
        lbNumero = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lbCount = new javax.swing.JLabel();
        lbPorcent = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btnPiloto = new javax.swing.JRadioButton();
        btnAutoC = new javax.swing.JCheckBox();
        btnNavegar = new javax.swing.JCheckBox();
        lbArq = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        lista = new javax.swing.JList<>();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        taJp = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        taGa = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        taFim = new javax.swing.JTextArea();
        jButton2 = new javax.swing.JButton();
        btnTravaGa = new javax.swing.JCheckBox();
        btnTravaFim = new javax.swing.JCheckBox();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        menuNovo = new javax.swing.JMenuItem();
        menuAbrir = new javax.swing.JMenuItem();
        menuSalvar = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                formFocusLost(evt);
            }
        });
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
                formWindowLostFocus(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        jPanel1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jPanel1KeyPressed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jButton1.setText("Salvar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        btnAutoSave.setText("auto save");

        btnAutoMini.setText("auto minimizar");

        lbNumero.setText("jLabel4");

        jLabel4.setText("/");

        lbCount.setText("jLabel5");

        lbPorcent.setText("jLabel5");

        jLabel5.setText("%");

        btnPiloto.setText("Piloto Automatico");
        btnPiloto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPilotoActionPerformed(evt);
            }
        });

        btnAutoC.setText("auto copiar");

        btnNavegar.setText("navegar");
        btnNavegar.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                btnNavegarStateChanged(evt);
            }
        });

        lbArq.setText("jLabel6");

        jLabel6.setText("Arquivo:");

        jButton3.setText("Vizualizar");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAutoSave)
                .addGap(18, 18, 18)
                .addComponent(btnAutoMini)
                .addGap(18, 18, 18)
                .addComponent(btnAutoC)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnNavegar)
                .addGap(32, 32, 32)
                .addComponent(lbNumero)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbCount)
                .addGap(18, 18, 18)
                .addComponent(lbPorcent)
                .addGap(42, 42, 42)
                .addComponent(jLabel5)
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbArq)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnPiloto)
                .addGap(71, 71, 71)
                .addComponent(jButton3)
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(btnAutoSave)
                    .addComponent(btnAutoMini)
                    .addComponent(lbNumero)
                    .addComponent(jLabel4)
                    .addComponent(lbCount)
                    .addComponent(lbPorcent)
                    .addComponent(jLabel5)
                    .addComponent(btnPiloto)
                    .addComponent(btnAutoC)
                    .addComponent(btnNavegar)
                    .addComponent(lbArq)
                    .addComponent(jLabel6)
                    .addComponent(jButton3))
                .addGap(0, 2, Short.MAX_VALUE))
        );

        lista.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lista.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        lista.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listaValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lista);

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        taJp.setEditable(false);
        taJp.setColumns(20);
        taJp.setRows(5);
        taJp.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                taJpMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(taJp);

        jLabel1.setText("original em japones");

        jLabel2.setText("google tradutor");

        taGa.setColumns(20);
        taGa.setRows(5);
        taGa.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                taGaMouseClicked(evt);
            }
        });
        taGa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                taGaKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                taGaKeyReleased(evt);
            }
        });
        jScrollPane3.setViewportView(taGa);

        jLabel3.setText("final");

        taFim.setColumns(20);
        taFim.setRows(5);
        taFim.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                taFimMouseClicked(evt);
            }
        });
        taFim.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                taFimPropertyChange(evt);
            }
        });
        taFim.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                taFimKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                taFimKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                taFimKeyTyped(evt);
            }
        });
        jScrollPane4.setViewportView(taFim);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ico/baixo.png"))); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        btnTravaGa.setText("travar");
        btnTravaGa.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                btnTravaGaStateChanged(evt);
            }
        });

        btnTravaFim.setText("travar");
        btnTravaFim.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                btnTravaFimStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE)
                            .addComponent(jScrollPane3)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jScrollPane4))
                        .addContainerGap())
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnTravaGa)
                        .addGap(40, 40, 40))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(234, 234, 234)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnTravaFim)
                        .addGap(32, 32, 32))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2))
                    .addComponent(btnTravaGa))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(btnTravaFim))
                        .addGap(9, 9, 9))
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 612, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jMenu1.setText("File");
        jMenu1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu1ActionPerformed(evt);
            }
        });

        menuNovo.setText("Novo");
        menuNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuNovoActionPerformed(evt);
            }
        });
        jMenu1.add(menuNovo);

        menuAbrir.setText("Abrir");
        menuAbrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAbrirActionPerformed(evt);
            }
        });
        jMenu1.add(menuAbrir);

        menuSalvar.setText("Salvar");
        menuSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSalvarActionPerformed(evt);
            }
        });
        jMenu1.add(menuSalvar);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void menuNovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuNovoActionPerformed
        
    }//GEN-LAST:event_menuNovoActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        atualiza();
        mostra();
        salvar();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void taFimPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_taFimPropertyChange
        
    }//GEN-LAST:event_taFimPropertyChange

    private void taFimKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_taFimKeyTyped
        
        
    }//GEN-LAST:event_taFimKeyTyped

    private void formFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusLost
        System.out.println("dei tab");
    }//GEN-LAST:event_formFocusLost

    private void formWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowLostFocus
        if(btnAutoSave.isSelected()){
            salvar();
            
        }
    }//GEN-LAST:event_formWindowLostFocus

    private void taGaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_taGaKeyPressed
        if(evt.getKeyCode() == KeyEvent.VK_TAB)
            btnNavegar.setSelected(true);
        if(navegar(evt.getKeyCode()))return;
        if(evt.getKeyCode()==KeyEvent.VK_ENTER)
           taFim.requestFocus();
    }//GEN-LAST:event_taGaKeyPressed

    private void listaValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listaValueChanged
        if(evt.getValueIsAdjusting())
            mostra();
    }//GEN-LAST:event_listaValueChanged

    private void taFimKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_taFimKeyPressed
        if(evt.getKeyCode() == KeyEvent.VK_TAB)
            btnNavegar.setSelected(true);
        if(navegar(evt.getKeyCode()))return;
        if(evt.getKeyCode() ==KeyEvent.VK_ENTER) {
            atualiza();
            proximo();
        }
    }//GEN-LAST:event_taFimKeyPressed
  
    private void btnPilotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPilotoActionPerformed
        if(btnPiloto.isSelected()){
            var ff = lista.getSelectedIndex();
            new Thread(()->{
                while(btnPiloto.isSelected()){
                    fraseAtual.traduz();
                    proximo();
                }
            }).start();
            for(var i =0; i<10; i++){
                final var f = ff + i;
                final var id = i;
                pool[i] = new Thread(()->{
                    var indice = f;
                    while(btnPiloto.isSelected()){
                        var frase = frases.get(indice);
                        System.out.println(id + "[" + indice + "]" +">>");
                        frase.traduz();
                        System.out.println(id + "[" + indice + "]" +"<<");
                        proximo();
                        indice += 10;
                    }
                });
                pool[i].start();
            }
        }else{
            for(var i =0; i<10; i++) {
                if(pool[i] != null)
                    try {
                        pool[i].join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Abridor.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
            taGa.setEditable(true);
            taFim.setEditable(true);
        }
    }//GEN-LAST:event_btnPilotoActionPerformed

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        System.out.println("keyyyy :" + evt.getKeyChar());
    }//GEN-LAST:event_formKeyPressed

    private void jPanel1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPanel1KeyPressed
         System.out.println("keyyyy :" + evt.getKeyChar());
    }//GEN-LAST:event_jPanel1KeyPressed

    private void taGaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_taGaKeyReleased
        if(evt.getKeyCode() == KeyEvent.VK_TAB)
            btnNavegar.setSelected(false);
    }//GEN-LAST:event_taGaKeyReleased

    private void taFimKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_taFimKeyReleased
        if(evt.getKeyCode() == KeyEvent.VK_TAB)
            btnNavegar.setSelected(false);
    }//GEN-LAST:event_taFimKeyReleased

    private void taGaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_taGaMouseClicked
        if(evt.getClickCount() >= 2)
            copiar(taGa.getText());
    }//GEN-LAST:event_taGaMouseClicked

    private void taFimMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_taFimMouseClicked
        if(evt.getClickCount() >= 2)
            copiar(taFim.getText());
    }//GEN-LAST:event_taFimMouseClicked

    private void taJpMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_taJpMouseClicked
        if(evt.getClickCount() >= 2)
            copiar(taJp.getText());
    }//GEN-LAST:event_taJpMouseClicked

    private void jMenu1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenu1ActionPerformed

    private void menuAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAbrirActionPerformed
        LeitorArquivos.setCurrentDirectory(new File("."));
        if(LeitorArquivos.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            var arq = LeitorArquivos.getSelectedFile();
            file = arq.getAbsolutePath();
            lbArq.setText(arq.getName());
            iniciaLista(file);
        }
        
    }//GEN-LAST:event_menuAbrirActionPerformed

    private void menuSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSalvarActionPerformed
        LeitorArquivos.setCurrentDirectory(new File(file));
        if(LeitorArquivos.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
            var arq = LeitorArquivos.getSelectedFile();
            if(!arq.getAbsolutePath().endsWith(".txt"))
                arq = new File(arq.getAbsoluteFile() + ".txt");
            file = arq.getAbsolutePath();
            lbArq.setText(arq.getName());
            salvar();
            System.out.println(arq);
        }
    }//GEN-LAST:event_menuSalvarActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        taFim.setText(taGa.getText());
        taFim.requestFocus();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void btnNavegarStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_btnNavegarStateChanged
       //taFim.setEditable(btnNavegar.isSelected());
       //taFim.setEditable(btnNavegar.isSelected());
    }//GEN-LAST:event_btnNavegarStateChanged

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
      btnNavegar.setSelected(false);
    }//GEN-LAST:event_formWindowGainedFocus

    private void btnTravaGaStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_btnTravaGaStateChanged
        taGa.setEditable(!btnTravaGa.isSelected());
    }//GEN-LAST:event_btnTravaGaStateChanged

    private void btnTravaFimStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_btnTravaFimStateChanged
        taFim.setEditable(!btnTravaFim.isSelected());
    }//GEN-LAST:event_btnTravaFimStateChanged

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        var txt = frases.stream()
                .map(Frase::getFim)
                .filter(s -> s.length() > 3)
                .collect(Collectors.joining("\n"));
        texto.escreve(txt);
        texto.setVisible(true);
    }//GEN-LAST:event_jButton3ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Abridor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Abridor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Abridor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Abridor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Abridor().setVisible(true);
            }
        });
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Variables">  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFileChooser LeitorArquivos;
    private javax.swing.JCheckBox btnAutoC;
    private javax.swing.JCheckBox btnAutoMini;
    private javax.swing.JCheckBox btnAutoSave;
    private javax.swing.JCheckBox btnNavegar;
    private javax.swing.JRadioButton btnPiloto;
    private javax.swing.JCheckBox btnTravaFim;
    private javax.swing.JCheckBox btnTravaGa;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lbArq;
    private javax.swing.JLabel lbCount;
    private javax.swing.JLabel lbNumero;
    private javax.swing.JLabel lbPorcent;
    private javax.swing.JList<String> lista;
    private javax.swing.JMenuItem menuAbrir;
    private javax.swing.JMenuItem menuNovo;
    private javax.swing.JMenuItem menuSalvar;
    private javax.swing.JTextArea taFim;
    private javax.swing.JTextArea taGa;
    private javax.swing.JTextArea taJp;
    // End of variables declaration//GEN-END:variables
    // </editor-fold> 
}

