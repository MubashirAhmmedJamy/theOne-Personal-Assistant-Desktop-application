package MotherContainer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.Timer; 
import java.sql.*;
import java.util.Stack;
import java.util.StringTokenizer;
import javax.swing.JFileChooser;
import javax.swing.table.DefaultTableModel;

public class MotherContainerFinal extends javax.swing.JFrame {
    //Routine Variables Begin
    String RoutineBegin;
    String RoutineEnd;
    String RoutineTask;
    String TaskDate;
    String bt, et;
    int bh, bm, eh, em;
    int hdif, mdif;
    String TaskLength;
    String sysTime, sysDate;
    Date d;
    String Message;
    SimpleDateFormat df, tf;
    boolean TI;
    boolean timeMatch;
    DefaultTableModel model;
    int BeginCode, EndCode;
    int bhA, ehA;
    //*************************************
//  StopWatch Variables
    int HC;
    int MC;
    int SC;
    boolean SI;
    boolean looper;
    boolean PI;
    String time;
    //StopWatch Variables End
    
    //Database Variables
    Connection con;
    Statement state, eState, mState;
    int exCount;
    ResultSet SysMatchResult, MessResult, loader;
    
    //Notebook variables*****************************
    PrintWriter print;
    String notes;
    String fileName;
    JFileChooser fc;
    //***********************************************
    
    //Calculator variables***************************
    String expression;
    int expLength;
    
    //CGPA Variables
    float credits;
    float results;
    int Count;    
   
    
    
    //The Mighty Constructor***********************
    public MotherContainerFinal() {
        setTitle("theOne");
        initComponents();
        SI = true;
        PI = true;
        TI = true;
        state = null;
        eState = null;
        mState = null;
        print = null;
        fileName = null;
        fc = new JFileChooser();
        model = (DefaultTableModel) TaskOverViewTable.getModel();
        expression = "";
        expressionField.setEditable(false);
        connector();
        systemTimeDatePicker();
        TableLoader();
    }
    
     public void CGPA(){
        float x, y;
        x = Float.parseFloat(credit.getSelectedItem().toString());
        credits += x;
        y = Float.parseFloat(result.getSelectedItem().toString());
        results += (x*y);
        Count++;
        cCount.setText(Integer.toString(Count + 1));
        cInput.setText(Integer.toString(Count));
    }
     
    public void CGPAcal(){
        float cgpa = results/credits;
        JOptionPane.showMessageDialog(rootPane, "CGPA: " + Float.toString(cgpa));
    }
    
    //Method: 1
    public void TableLoader(){
        String taskTime, task, length;
        try {
            loader = eState.executeQuery("select RoutineBegin, RoutineEnd, RoutineTask, TaskLength from routine");
            while(loader.next()){
                 taskTime = loader.getString("RoutineBegin")+" to "+loader.getString("RoutineEnd");
                 task = loader.getString("RoutineTask");
                 length = loader.getString("TaskLength");
                 String A [] = {taskTime,task,length};
                 model.addRow(A);
                 //System.out.println("Loaded Successfully!");
            }
            
        } catch (Exception e) {
            System.out.println("Problem found when loading!");
        }        
    } 
    
    //Method: 2**********************************
    public String refineBegin(String s){
        return s.substring(0, s.indexOf(' '));
    }
    
    //Method:3 Connects to the Database the_one
    
    private void connector(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/the_one", "root", "paradise46");
            state = con.createStatement();
            eState = con.createStatement();
            mState = con.createStatement();
            exCount = 0;
            //System.out.println("Connected!");
        } catch (Exception ex) {
        }
    }
    
    //Method: 4**************************
    public int InsertRoutineValues(){
        String query = "insert into routine(TaskDate,BeginCode,EndCode,RoutineBegin,RoutineEnd, RoutineTask, TaskLength) values ('"+TaskDate+"',"+BeginCode+","+EndCode+",'"+RoutineBegin+"','"+RoutineEnd+"','"+RoutineTask+"','"+ TaskLength+"')";
        //System.out.println(query);
        try {
            return state.executeUpdate(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }        
        return 0;
    }
    
    // Method: 5 (Time Picking)
    public void systemTimeDatePicker() {
        new Timer(0, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                d = new Date();
                tf = new SimpleDateFormat("h.ma");
                df = new SimpleDateFormat("dd/MM/yy");
                sysTime = tf.format(d);
                sysDate = df.format(d);
                
                try {
                    SysMatchResult = eState.executeQuery("select RoutineBegin from routine where RoutineBegin = '" + sysTime + "'");

                    if (SysMatchResult.next()) {
                        MessResult = mState.executeQuery("select RoutineBegin, RoutineEnd, RoutineTask, TaskLength from routine where RoutineBegin = '"+sysTime+"'");
                        
                        //System.out.println("Match found!\n");
                                                
                        JOptionPane.showMessageDialog(rootPane, messageCreator(MessResult)); 
                        eState.executeUpdate("delete from routine where RoutineBegin = '"+sysTime+"'");
                        Cleaner();
                    }
                    
                    SysMatchResult = eState.executeQuery("select * from routine");
                    
                    if (SysMatchResult.next()) {
                        SysMatchResult = eState.executeQuery("select TaskDate from routine where TaskDate = '" + sysDate + "'");
                        if (!SysMatchResult.next()) {
                            System.out.println("Table dies!");
                            eState.executeUpdate("delete from routine");
                        }
                    }
                    
                } catch (SQLException ex) {
                    
                }
            }
        }).start();
    }
    
    
    //Method: 6***************************************
    public String messageCreator(ResultSet rs){
        String mess = "Scheduled Task: ", value = "";
        
        try {
            while(rs.next()){
                mess += rs.getString("RoutineTask") + "\nScheduled Time: ";
                mess += rs.getString("RoutineBegin") + " to " + rs.getString("RoutineEnd") + "\nTime Length: ";
                mess += rs.getString("TaskLength") + "\n\n";
                
                value = rs.getString("RoutineBegin")+" to " + rs.getString("RoutineEnd");
            }
            
            //System.out.println(value);
            try {
                deleteRowByValue(value);
            } catch (Exception e) {
            }
            
            
            //System.out.println("Message Created: "+ mess);
            
            return mess;
        } catch (SQLException ex) {
        }
        return null;
    }
    
    
// Method: 7 *********************************
    public boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
//  Method: 8 ********************************
    public void Cleaner() {
        RoutineBegin = "";
        RoutineEnd = "";
        RoutineTask = "";
        TaskDate = "";
        bh = 0;
        bm = 0;
        bt = "";
        eh = 0;
        em = 0;
        et = "";
    }
    
// Method: 9 *********************************    
    public void Clearer(){
        bhS.setText("");
        bmS.setText("");
        ehS.setText("");
        emS.setText("");
        taskPicker.setText("");
        btS.setSelectedItem("AM");
        etS.setSelectedItem("AM");
    }
// Method: 10 *********************************    
    
    public boolean getRoutineValues() { 
        Cleaner();
        for (int i = 0; i <= taskPanel.getComponentCount(); i++) {
            if (i <= 1) {
                JTextField tf = (JTextField)taskPanel.getComponent(i);

                boolean CI = isInteger(tf.getText());

                if (CI) {
                    if (i == 0 && (Integer.parseInt(tf.getText()) > 12 || Integer.parseInt(tf.getText()) < 0)) {
                        JOptionPane.showMessageDialog(rootPane, "Beginning hour is invalid!");
                        Cleaner();
                        //Clearer();
                        bhS.requestFocus();
                        return false;
                    } else if (i == 1 && (Integer.parseInt(tf.getText()) > 60 || Integer.parseInt(tf.getText()) < 0)) {
                        JOptionPane.showMessageDialog(rootPane, "Beginning minute is invalid!");
                        Cleaner();
                        //Clearer();
                        bmS.requestFocus();
                        return false;
                    }
                    RoutineBegin += Integer.toString(Integer.parseInt(tf.getText()));

                    if (i == 0) {
                        //System.out.println("Getting Begin Hour: " + tf.getText());
                        bh = Integer.parseInt(tf.getText());
                    } else {
                        bm = Integer.parseInt(tf.getText());
                    }

                } else {
                    if (i == 1 && tf.getText().equals("")) {
                        RoutineBegin += "0";
                        bm = 0;
                    } else {
                        JOptionPane.showMessageDialog(rootPane, "Invalid input!\n"
                                + "BeginHour and BeginMinutes must be Integers!");
                        Cleaner();
                        //Clearer();
                        bhS.requestFocus();
                        return false;
                    }
                }

                if (i == 0) {
                    RoutineBegin += ".";
                }

            } else if (i == 2) {
                JComboBox ch = (JComboBox)taskPanel.getComponent(i);

                RoutineBegin += ch.getSelectedItem().toString();
                bt = ch.getSelectedItem().toString();
            } else if (i == 4 || i == 5) {
                JTextField tf = (JTextField)taskPanel.getComponent(i);
                boolean CI = isInteger(tf.getText());

                if (CI) {
                    if (i == 4 && (Integer.parseInt(tf.getText()) > 12 || Integer.parseInt(tf.getText()) < 0)) {
                        JOptionPane.showMessageDialog(rootPane, "Ending hour is invalid!");
                        Cleaner();
                        //Clearer();
                        ehS.requestFocus();
                        return false;
                    } else if (i == 5 && (Integer.parseInt(tf.getText()) > 60 || Integer.parseInt(tf.getText()) < 0)) {
                        JOptionPane.showMessageDialog(rootPane, "Ending minute is invalid!");
                        Cleaner();
                        //Clearer();
                        emS.requestFocus();
                        return false;
                    }

                    RoutineEnd += Integer.toString(Integer.parseInt(tf.getText()));
                    if (i == 4){
                        //System.out.println("Getting end Hour: " + tf.getText());
                        eh = Integer.parseInt(tf.getText());
                        
                        //System.out.println("Checking eh: " + eh);
                    } else {
                        em = Integer.parseInt(tf.getText());
                    }

                } else {
                    if (i == 5 && tf.getText().equals("")) {
                        RoutineEnd += "0";
                        em = 0;
                    } else {
                        JOptionPane.showMessageDialog(rootPane, "Invalid input!\n"
                                + "BeginHour and BeginMinutes must be Integers!");
                        //Clearer();
                        Cleaner();
                        bhS.requestFocus();
                        return false;
                    }
                }

                if (i == 4) {
                    RoutineEnd += ".";
                }

            } else if (i == 6) {
                JComboBox ch = (JComboBox) taskPanel.getComponent(i);
                RoutineEnd += ch.getSelectedItem().toString();
                et = ch.getSelectedItem().toString();

            } else if (i == 8) {
                JTextField tf = (JTextField) taskPanel.getComponent(i);
                RoutineTask = tf.getText();
            }
        }
        
        if(lengthFinder() == false){
            return false;
        }
        
        TaskLength = Integer.toString(hdif) + "hrs & " +Integer.toString(mdif) + "mins";
        BeginCode = (bhA*100) + bm;
        EndCode = (ehA*100) + em;
        return true;
    }
    
//  Method: 11
    public boolean lengthFinder(){   
        if(bt.equals("AM")){
            bhA = bh;
            if(bh == 12){
                 bhA = 0;
            }    
        }else if(bt.equals("PM")){
            bhA = bh;            
            if(bh < 12){
                bhA = bh + 12;
            }            
            //System.out.println("bh: "+bhA);
        }
        
        
        if(et.equals("AM")){
            ehA = eh;
            if(eh == 12){
                 if(em == 0){
                     ehA = 24;
                 }else{
                     ehA = 0;
                 }
            }    
        }else if(et.equals("PM")){
            ehA = eh;
            if(eh<12){
                ehA = eh + 12;
            }
            //System.out.println("eh: "+ehA);
        }
        
        if (bm == 60) {
            if (bh != 12) {
                bm = 0;
                bhA++;
                bh++;
                RoutineBegin = Integer.toString(bh) + ".0"+ bt;
                System.out.println("BeginTime = " + RoutineBegin);
            } else if (bh == 12) {
                if (bt.equals("AM")) {
                    bhA = 1;
                } else {
                    bhA = 13;
                }
                bm = 0;
                RoutineBegin = "1.0" + bt;
                System.out.println("BeginTime = " + RoutineBegin);
            }
        }
        if (em == 60) {
            if (eh != 12) {
                em = 0;
                ehA++;
                eh++;
                RoutineEnd = Integer.toString(eh) + ".0" + et;
                System.out.println("EndTime = " + RoutineEnd);
            } else if (eh == 12) {
                if (et.equals("AM")) {
                    ehA = 1;
                } else {
                    ehA = 13;
                }
                em = 0;
                RoutineEnd = "1.0" + et;
                System.out.println("EndTime = " + RoutineEnd);
            }
        }

        
        if (ehA < bhA) {
            //System.out.println("Hello");
            JOptionPane.showMessageDialog(rootPane, "Try to make it within Today!");
            bhS.requestFocus();
            //Clearer();
            Cleaner();
            return false;
        }

        
        
        hdif = ehA - bhA;
        
        if(hdif == 0){
            if(em < bm){
                JOptionPane.showMessageDialog(rootPane, "Try to make it within today!");
                Cleaner();
                //Clearer();
                return false;
            }else{
                mdif = em - bm;
            }
            return true;
        }
        
        
        if(em < bm){
            hdif--;
            mdif = 60 - bm + em;
        } else if(em >= bm){
            mdif = em - bm;
        }
            
        return true;
    }
    
    //Method: 12 (Stopwatch)
    
    public void StopWatchtimer(int x) {
        Thread timer = new Thread() {
            @Override
            public void run() {
                while (looper) {
                    time = "           " + Integer.toString(HC);
                    
                    if(HC == 0){
                        time += "0";
                    }
                    time += ":" + Integer.toString(MC);
                    
                    if(MC == 0){
                        time += "0";
                    }
                    time += ":" + Integer.toString(SC);
                    
                    if(SC == 0){
                        time += "0";
                    }
                    
                    swLabel.setText(time);
                    
                    
                    SC++;
                    if (SC == 60) {
                        SC = 0;
                        MC++;
                        if (MC == 60) {
                            HC++;
                            MC = 0;
                        }
                    }
                    try {
                        sleep(1000);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };

        if (x == 1 && SI) {
            SI = false;
            looper = true;
            timer.start();
        } else if (x == 2 && PI && SI == false) {
            looper = false;
            swPause.setText("resume");
            PI = false;
        } else if (x == 2 && PI == false) {
            SI = false;
            looper = true;
            swPause.setText("pause");
            PI = true;
            timer.start();
        } else if (x == 3) {
            HC = 0;
            MC = 0;
            SC = 0;
            swLabel.setText("           00:00:00");
            swPause.setText("pause");
            if(PI == false){
                SI = true;
            }
            PI = true;
        }
    }
    
    //Method: 13
    public void addToTable(String A[]){
        model.addRow(A);
    }
    
    //Method: 14
    void deleteRowByValue(Object value) {
        for (int i = model.getRowCount() - 1; i >= 0; --i) {
            for (int j = model.getColumnCount() - 1; j >= 0; --j) {
                if (model.getValueAt(i, j).equals(value)) {
                    //System.out.println("Match foun in the Table. At row: " + i);
                    //System.out.println("Deleting the row in the table.");
                    model.removeRow(i);
                }
            }
        }
    }

    public void calculate(String exp){
        int exlen = exp.length();
        String tokens[] = new String[100];
        int toCount = 0;
        int x = 0;
        char s[] = new char[100];
        int beginIndex = 0;
        for(int i = 0; i < exlen - 1; i++){
            if((exp.charAt(i) == '+')||(exp.charAt(i) == '-')||(exp.charAt(i) == '*')||(exp.charAt(i) == '/')||(exp.charAt(i) == '^')){
                x++;
                tokens[toCount] = exp.substring(beginIndex, i);
                s[toCount] = exp.charAt(i);
                toCount++;
                beginIndex = i+1;
            }
        }
        x++;
        
        tokens[toCount] = exp.substring(beginIndex, exlen);
        float res = 0;
        for(int i = 0; i < x; i++){
            if(i == 0){
                res = Float.parseFloat(tokens[i]);
            }
                    
            switch(s[i]){
                case '+':
                    res += Float.parseFloat(tokens[i+1]);
                    break;
                case '-':
                    res -= Float.parseFloat(tokens[i+1]);
                    break;
                case '*':
                    res *= Float.parseFloat(tokens[i+1]);
                    break;
                case '/':
                    res /= Float.parseFloat(tokens[i+1]);
                    break;
            }
        }
        
        RES.setText(Float.toString(res));
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton10 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        taskPanel = new javax.swing.JPanel();
        bhS = new javax.swing.JTextField();
        bmS = new javax.swing.JTextField();
        etS = new javax.swing.JComboBox();
        to = new javax.swing.JLabel();
        taskPicker = new javax.swing.JTextField();
        emS = new javax.swing.JTextField();
        ehS = new javax.swing.JTextField();
        btS = new javax.swing.JComboBox();
        assign = new javax.swing.JLabel();
        createLabel = new javax.swing.JLabel();
        clearFields = new javax.swing.JButton();
        addTask = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        TaskOverViewTable = new javax.swing.JTable();
        deleteTask = new javax.swing.JButton();
        addTask1 = new javax.swing.JButton();
        addTask2 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        cInput = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        credit = new javax.swing.JComboBox();
        result = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        cCount = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        CGPAcalculate = new javax.swing.JButton();
        cClean = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        stopWatchPanel = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        swLabel = new javax.swing.JLabel();
        swPause = new javax.swing.JButton();
        swStart = new javax.swing.JButton();
        swReset = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        noteBook = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        noteSave = new javax.swing.JButton();
        open = new javax.swing.JButton();
        noteSaveAs = new javax.swing.JButton();
        clear = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        calButtonPanel = new javax.swing.JPanel();
        seven = new javax.swing.JButton();
        nine = new javax.swing.JButton();
        eight = new javax.swing.JButton();
        five = new javax.swing.JButton();
        six = new javax.swing.JButton();
        two = new javax.swing.JButton();
        four = new javax.swing.JButton();
        one = new javax.swing.JButton();
        three = new javax.swing.JButton();
        zero = new javax.swing.JButton();
        point = new javax.swing.JButton();
        equals = new javax.swing.JButton();
        divide = new javax.swing.JButton();
        multiply = new javax.swing.JButton();
        minus = new javax.swing.JButton();
        plus = new javax.swing.JButton();
        answer = new javax.swing.JButton();
        backSpace = new javax.swing.JButton();
        sqrt = new javax.swing.JButton();
        power = new javax.swing.JButton();
        clearExpression = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        RES = new javax.swing.JLabel();
        expressionField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setLocation(new java.awt.Point(400, 25));
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(0, 153, 255));

        jPanel7.setBackground(new java.awt.Color(0, 102, 255));

        jLabel1.setBackground(new java.awt.Color(0, 102, 102));
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/theme.JPG"))); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(43, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 546, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE)
        );

        jButton10.setBackground(new java.awt.Color(0, 51, 102));
        jButton10.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton10.setText("About");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(70, 70, 70)
                        .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 520, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(37, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39))
        );

        jTabbedPane1.addTab("Home", jPanel1);

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));

        taskPanel.setBackground(new java.awt.Color(204, 204, 204));

        bhS.setFont(new java.awt.Font("Siyam Rupali", 0, 11)); // NOI18N
        bhS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bhSActionPerformed(evt);
            }
        });

        bmS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bmSActionPerformed(evt);
            }
        });

        etS.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "AM", "PM" }));
        etS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                etSActionPerformed(evt);
            }
        });

        to.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        to.setText("to");

        taskPicker.setFont(new java.awt.Font("Siyam Rupali", 0, 11)); // NOI18N
        taskPicker.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                taskPickerActionPerformed(evt);
            }
        });
        taskPicker.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                taskPickerKeyTyped(evt);
            }
        });

        emS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emSActionPerformed(evt);
            }
        });

        ehS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ehSActionPerformed(evt);
            }
        });

        btS.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "AM", "PM" }));
        btS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSActionPerformed(evt);
            }
        });

        assign.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        assign.setText("->");

        javax.swing.GroupLayout taskPanelLayout = new javax.swing.GroupLayout(taskPanel);
        taskPanel.setLayout(taskPanelLayout);
        taskPanelLayout.setHorizontalGroup(
            taskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(taskPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bhS, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bmS, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(btS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(to, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ehS, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(emS, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(etS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addComponent(assign, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(taskPicker, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        taskPanelLayout.setVerticalGroup(
            taskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(taskPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(taskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(taskPicker, javax.swing.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
                    .addComponent(bmS)
                    .addComponent(bhS)
                    .addComponent(btS)
                    .addComponent(to, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ehS)
                    .addComponent(emS)
                    .addComponent(etS)
                    .addComponent(assign, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        createLabel.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        createLabel.setForeground(new java.awt.Color(255, 255, 255));
        createLabel.setText("                Schedule a task for today");

        clearFields.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        clearFields.setText("Clear Fields");
        clearFields.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearFieldsActionPerformed(evt);
            }
        });

        addTask.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        addTask.setText("Add Task");
        addTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTaskActionPerformed(evt);
            }
        });

        TaskOverViewTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Scheduled Time", "Task", "Time Length"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(TaskOverViewTable);

        deleteTask.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        deleteTask.setText("Delete");
        deleteTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteTaskActionPerformed(evt);
            }
        });

        addTask1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        addTask1.setText("Sort Tasks");
        addTask1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTask1ActionPerformed(evt);
            }
        });

        addTask2.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        addTask2.setText("Delete All");
        addTask2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTask2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(74, 74, 74)
                        .addComponent(createLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 537, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(addTask2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(addTask1, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane3)
                            .addComponent(taskPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(clearFields, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addTask, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deleteTask, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(37, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(createLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(taskPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addTask, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearFields, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteTask, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addTask1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addTask2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Task Manager", jPanel2);

        jPanel3.setBackground(new java.awt.Color(51, 51, 0));

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel6.setText("  Input taken:");
        jLabel6.setOpaque(true);

        cInput.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        cInput.setText("0");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addComponent(jLabel6)
                .addGap(18, 18, 18)
                .addComponent(cInput)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                    .addComponent(cInput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        credit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "1.5", "2", "3", "4", "5" }));

        result.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2.00", "2.25", "2.50", "2.75", "3.00", "3.25", "3.50", "3.75", "4.00" }));

        jLabel9.setText("Course no");

        jLabel10.setText("     Credits");

        jLabel11.setText("    Result");

        cCount.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        cCount.setText("1");

        jButton2.setText("Insert");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(cCount, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addGap(32, 32, 32)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel10)
                    .addComponent(credit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(result, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton2))
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cCount, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(result, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(credit, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(38, 38, 38))
        );

        CGPAcalculate.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        CGPAcalculate.setText("Calculate CGPA");
        CGPAcalculate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CGPAcalculateActionPerformed(evt);
            }
        });

        cClean.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        cClean.setText("Clean");
        cClean.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cCleanActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel7.setText("            Insert credit and corresponding result of");
        jLabel7.setOpaque(true);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(130, 130, 130)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addComponent(cClean, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(CGPAcalculate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(158, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(95, 95, 95)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CGPAcalculate, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cClean, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(132, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("CGPA Calculator", jPanel3);

        stopWatchPanel.setBackground(new java.awt.Color(0, 51, 153));

        jPanel11.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        swLabel.setFont(new java.awt.Font("Meiryo UI", 1, 24)); // NOI18N
        swLabel.setText("           00:00:00");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(swLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(42, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(swLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        swPause.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        swPause.setText("pause");
        swPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                swPauseActionPerformed(evt);
            }
        });

        swStart.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        swStart.setText("start");
        swStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                swStartActionPerformed(evt);
            }
        });

        swReset.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        swReset.setText("reset");
        swReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                swResetActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel5.setText("h:m:s");

        javax.swing.GroupLayout stopWatchPanelLayout = new javax.swing.GroupLayout(stopWatchPanel);
        stopWatchPanel.setLayout(stopWatchPanelLayout);
        stopWatchPanelLayout.setHorizontalGroup(
            stopWatchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stopWatchPanelLayout.createSequentialGroup()
                .addGap(82, 82, 82)
                .addComponent(swPause, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 107, Short.MAX_VALUE)
                .addComponent(swReset, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(106, 106, 106)
                .addComponent(swStart, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(82, 82, 82))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stopWatchPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(143, 143, 143))
            .addGroup(stopWatchPanelLayout.createSequentialGroup()
                .addGap(280, 280, 280)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        stopWatchPanelLayout.setVerticalGroup(
            stopWatchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stopWatchPanelLayout.createSequentialGroup()
                .addGap(69, 69, 69)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addGroup(stopWatchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(swStart, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(swReset, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(swPause, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(293, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Stopwatch", stopWatchPanel);

        jPanel6.setBackground(new java.awt.Color(153, 0, 51));

        noteBook.setBackground(new java.awt.Color(0, 0, 0));
        noteBook.setColumns(20);
        noteBook.setFont(new java.awt.Font("Siyam Rupali", 0, 18)); // NOI18N
        noteBook.setForeground(new java.awt.Color(255, 255, 255));
        noteBook.setRows(5);
        jScrollPane1.setViewportView(noteBook);

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel4.setText("   Write Here");
        jLabel4.setOpaque(true);

        jPanel4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        noteSave.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        noteSave.setText("Save");
        noteSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noteSaveActionPerformed(evt);
            }
        });

        open.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        open.setText("Open");
        open.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openActionPerformed(evt);
            }
        });

        noteSaveAs.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        noteSaveAs.setText("Save as");
        noteSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noteSaveAsActionPerformed(evt);
            }
        });

        clear.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        clear.setText("Clear");
        clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(clear, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60)
                .addComponent(noteSaveAs, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                .addComponent(noteSave, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60)
                .addComponent(open, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(noteSave)
                    .addComponent(open)
                    .addComponent(noteSaveAs)
                    .addComponent(clear))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(198, 198, 198)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 469, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Notebook", jPanel6);

        jPanel9.setBackground(new java.awt.Color(0, 0, 51));

        calButtonPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        seven.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        seven.setText("7");
        seven.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sevenActionPerformed(evt);
            }
        });

        nine.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        nine.setText("9");
        nine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nineActionPerformed(evt);
            }
        });

        eight.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        eight.setText("8");
        eight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eightActionPerformed(evt);
            }
        });

        five.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        five.setText("5");
        five.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fiveActionPerformed(evt);
            }
        });

        six.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        six.setText("6");
        six.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sixActionPerformed(evt);
            }
        });

        two.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        two.setText("2");
        two.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                twoActionPerformed(evt);
            }
        });

        four.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        four.setText("4");
        four.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fourActionPerformed(evt);
            }
        });

        one.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        one.setText("1");
        one.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                oneActionPerformed(evt);
            }
        });

        three.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        three.setText("3");
        three.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                threeActionPerformed(evt);
            }
        });

        zero.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        zero.setText("0");
        zero.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zeroActionPerformed(evt);
            }
        });

        point.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        point.setText(".");
        point.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pointActionPerformed(evt);
            }
        });

        equals.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        equals.setText("=");
        equals.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                equalsActionPerformed(evt);
            }
        });

        divide.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        divide.setText("/");
        divide.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                divideActionPerformed(evt);
            }
        });

        multiply.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        multiply.setText("*");
        multiply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                multiplyActionPerformed(evt);
            }
        });

        minus.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        minus.setText("-");
        minus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minusActionPerformed(evt);
            }
        });

        plus.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        plus.setText("+");
        plus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plusActionPerformed(evt);
            }
        });

        answer.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        answer.setText("Ans");

        backSpace.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        backSpace.setText("back");

        sqrt.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        sqrt.setText("√");

        power.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        power.setText("^");
        power.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                powerActionPerformed(evt);
            }
        });

        clearExpression.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        clearExpression.setText("Clear");
        clearExpression.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearExpressionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout calButtonPanelLayout = new javax.swing.GroupLayout(calButtonPanel);
        calButtonPanel.setLayout(calButtonPanelLayout);
        calButtonPanelLayout.setHorizontalGroup(
            calButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(calButtonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(calButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(calButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(seven, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(one, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(four, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(point, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(calButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(calButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(zero, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                        .addComponent(two, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                        .addComponent(eight, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE))
                    .addComponent(five, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(calButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(calButtonPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(equals, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(plus, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(answer, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(23, 23, 23))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, calButtonPanelLayout.createSequentialGroup()
                        .addGroup(calButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, calButtonPanelLayout.createSequentialGroup()
                                .addComponent(three, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(minus, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(sqrt, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, calButtonPanelLayout.createSequentialGroup()
                                .addGroup(calButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(nine, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(six, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(calButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(calButtonPanelLayout.createSequentialGroup()
                                        .addComponent(multiply, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(power, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(calButtonPanelLayout.createSequentialGroup()
                                        .addComponent(divide, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(calButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(backSpace, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(clearExpression, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        calButtonPanelLayout.setVerticalGroup(
            calButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(calButtonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(calButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(eight, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                    .addComponent(seven, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                    .addComponent(divide, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                    .addComponent(nine, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                    .addGroup(calButtonPanelLayout.createSequentialGroup()
                        .addComponent(backSpace)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(clearExpression)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(calButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(four, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(five, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(six, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(calButtonPanelLayout.createSequentialGroup()
                        .addGroup(calButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(multiply, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(power, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(calButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(one, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(two, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(three, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(calButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(minus, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(sqrt, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(calButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(zero, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                    .addComponent(point, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                    .addComponent(equals, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                    .addGroup(calButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(plus, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                        .addComponent(answer, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jLabel2.setBackground(new java.awt.Color(0, 0, 51));
        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 153, 204));
        jLabel2.setText("Insert Expresion");
        jLabel2.setOpaque(true);

        RES.setBackground(new java.awt.Color(255, 255, 255));
        RES.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        RES.setForeground(new java.awt.Color(51, 204, 255));
        RES.setToolTipText("");
        RES.setOpaque(true);

        expressionField.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        expressionField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expressionFieldActionPerformed(evt);
            }
        });
        expressionField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                expressionFieldKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(231, 231, 231)
                        .addComponent(jLabel2))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(calButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 582, Short.MAX_VALUE)
                            .addComponent(RES, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(expressionField))))
                .addContainerGap(45, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(expressionField, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(RES, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(calButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );

        jTabbedPane1.addTab("Calculator", jPanel9);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 682, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 642, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bhSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bhSActionPerformed
        bmS.requestFocus();
    }//GEN-LAST:event_bhSActionPerformed

    private void bmSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bmSActionPerformed
        btS.requestFocus();
    }//GEN-LAST:event_bmSActionPerformed

    private void btSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSActionPerformed
        ehS.requestFocus();
    }//GEN-LAST:event_btSActionPerformed

    private void ehSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ehSActionPerformed
        emS.requestFocus();
    }//GEN-LAST:event_ehSActionPerformed

    private void emSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emSActionPerformed
        etS.requestFocus();
    }//GEN-LAST:event_emSActionPerformed

    private void etSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_etSActionPerformed
        taskPicker.requestFocus();
    }//GEN-LAST:event_etSActionPerformed
    
  
    private void taskPickerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_taskPickerActionPerformed
        boolean d = getRoutineValues();

        if (d) {
            
            try {
                SysMatchResult = eState.executeQuery("select RoutineBegin from routine where RoutineBegin = '" + RoutineBegin + "'");
                if (!SysMatchResult.next()) {
                    TaskDate = sysDate;
//                    System.out.println("Successfully Picked!");
//                    System.out.print(RoutineBegin + " to " + RoutineEnd);
//                    System.out.println(": " + RoutineTask + " (" + TaskLength + ")");
//
//                    System.out.println("Inserting at:" + TaskDate + "    " + sysTime);
//                    System.out.println(TaskDate);
                    exCount = InsertRoutineValues();
                    //System.out.println("Adding to table!");
                    String A[] = {RoutineBegin+" to "+RoutineEnd,RoutineTask,TaskLength};
                    addToTable(A);
                    if (exCount > 0) {
                        JOptionPane.showMessageDialog(rootPane, "Task Scheduled Successfully.");
                    }
                }else{
                    JOptionPane.showMessageDialog(rootPane, "Another Task is starting at "+RoutineBegin+"\nTask not Scheduled!");
                }
                
            } catch (Exception e) {                
            }

        }
    }//GEN-LAST:event_taskPickerActionPerformed

    private void taskPickerKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_taskPickerKeyTyped
        if (taskPicker.getText().length() >= 35) {
            evt.consume();
        }
    }//GEN-LAST:event_taskPickerKeyTyped

    private void threeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_threeActionPerformed
        if (expLength <= 35) {
            expression += "3";
            expressionField.setText(expression);
            expLength++;
        }
    }//GEN-LAST:event_threeActionPerformed

    private void plusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plusActionPerformed
        expression += "+";
        expressionField.setText(expression);
    }//GEN-LAST:event_plusActionPerformed

    private void swStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_swStartActionPerformed
        StopWatchtimer(1);
    }//GEN-LAST:event_swStartActionPerformed

    private void swResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_swResetActionPerformed
        StopWatchtimer(3);
    }//GEN-LAST:event_swResetActionPerformed

    private void swPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_swPauseActionPerformed
        StopWatchtimer(2);
    }//GEN-LAST:event_swPauseActionPerformed

    private void clearFieldsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFieldsActionPerformed
        Clearer();
        Cleaner();
        bhS.requestFocus();
    }//GEN-LAST:event_clearFieldsActionPerformed

    private void addTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTaskActionPerformed
        boolean d = getRoutineValues();

        if (d) {

            try {
                SysMatchResult = eState.executeQuery("select RoutineBegin from routine where RoutineBegin = '" + RoutineBegin + "'");
                if (!SysMatchResult.next()) {
                    TaskDate = sysDate;
                    //                    System.out.println("Successfully Picked!");
                    //                    System.out.print(RoutineBegin + " to " + RoutineEnd);
                    //                    System.out.println(": " + RoutineTask + " (" + TaskLength + ")");
                    //
                    //                    System.out.println("Inserting at:" + TaskDate + "    " + sysTime);
                    //                    System.out.println(TaskDate);
                    exCount = InsertRoutineValues();
                    //System.out.println("Adding to table!");
                    String A[] = {RoutineBegin+" to "+RoutineEnd,RoutineTask,TaskLength};
                    addToTable(A);
                    if (exCount > 0) {
                        JOptionPane.showMessageDialog(rootPane, "Task Scheduled Successfully.");
                    }
                }else{
                    JOptionPane.showMessageDialog(rootPane, "Another Task is starting at "+RoutineBegin+"\nTask not Scheduled!");
                }

            } catch (Exception e) {
            }

        }
    }//GEN-LAST:event_addTaskActionPerformed

    private void deleteTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteTaskActionPerformed
        int confirm;
        try {
            int x = TaskOverViewTable.getSelectedRow();
            model = (DefaultTableModel) TaskOverViewTable.getModel();
            String value = (String) model.getValueAt(x, 0), s1;
            int spaceAt = value.indexOf(' ');
            s1 = value.substring(0, spaceAt);
            //System.out.println("Selected begin time: " + s1);
            
            if(x >= 0){
                confirm = JOptionPane.showConfirmDialog(rootPane, "Delete this task?","Task Remover", JOptionPane.OK_CANCEL_OPTION);
                
                if (confirm == 0) {
                    try {
                        //System.out.println("Query: "+"delete from routine where RoutineBegin = '"+s1+"'");
                        eState.executeUpdate("delete from routine where RoutineBegin = '"+s1+"'");
                        //System.out.println("Deleted from Database!");
                        model.removeRow(x);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, "No task is selected for deleting!");
        }
    }//GEN-LAST:event_deleteTaskActionPerformed

    private void addTask1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTask1ActionPerformed
        int x = TaskOverViewTable.getRowCount();
        System.out.println("Total row: "+x);
        while(TaskOverViewTable.getRowCount() != 0){
             model.removeRow(0);
        }
        TableLoader();
    }//GEN-LAST:event_addTask1ActionPerformed

    private void addTask2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTask2ActionPerformed
        int y = JOptionPane.showConfirmDialog(rootPane, "Delete all tasks?", "Hey Wait!", JOptionPane.OK_CANCEL_OPTION);
        if (y == 0) {
            int x = TaskOverViewTable.getRowCount();
            System.out.println("Total row: " + x);
            while (TaskOverViewTable.getRowCount() != 0) {
                model.removeRow(0);
            }

            try {
                eState.executeUpdate("delete from routine");
                System.out.println("All task deleted!");
            } catch (Exception e) {
            }
        }
    }//GEN-LAST:event_addTask2ActionPerformed

    private void noteSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noteSaveActionPerformed
        int x = -1, a = -1;
        try {
            if (fileName == null) {
                x = fc.showSaveDialog(this);
                if (x == 0) {
                    print = new PrintWriter(new FileWriter(fc.getSelectedFile()));
                    notes = noteBook.getText();
                    StringTokenizer st = new StringTokenizer(notes, System.getProperty("line.separator"));
                    while (st.hasMoreElements()) {
                        print.println(st.nextToken());
                    }

                    JOptionPane.showMessageDialog(rootPane, "Note Saved");
                    fileName = fc.getSelectedFile().getName();
                }
            } else {
                a = JOptionPane.showConfirmDialog(rootPane, "Do you want to edit the note '" + fileName + "'?", "Warning", JOptionPane.OK_CANCEL_OPTION);
                if (a == 0) {
                    print = new PrintWriter(new FileWriter(fc.getSelectedFile()));
                    notes = noteBook.getText();
                    StringTokenizer st = new StringTokenizer(notes, System.getProperty("line.separator"));
                    
                    while (st.hasMoreElements()) {
                        print.println(st.nextToken());
                    }
                    
                    JOptionPane.showMessageDialog(rootPane, "The note '" + fileName + "' has been edited");
                }
            }
        } catch (Exception e) {
        } finally {
            if (x == 0 || a == 0) {
                print.close();
            }
        }

    }//GEN-LAST:event_noteSaveActionPerformed

    private void openActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openActionPerformed
        int x = -1;
        
        try {
            x = fc.showOpenDialog(this);
            if(x == 0){
                noteBook.setText("");
                Reader in = new FileReader(fc.getSelectedFile());
                char buff[] = new char[100000000];
                int nch;
                while((nch = in.read(buff, 0, buff.length)) != -1){
                    noteBook.append(new String(buff, 0, nch));
                }
                fileName = fc.getSelectedFile().getName();
            }
        } catch (Exception e) {
        }
    }//GEN-LAST:event_openActionPerformed

    private void noteSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noteSaveAsActionPerformed
        int x = -1;
        try {
            x = fc.showSaveDialog(this);
            if (x == 0) {
                print = new PrintWriter(new FileWriter(fc.getSelectedFile()));
                notes = noteBook.getText();
                StringTokenizer st = new StringTokenizer(notes, System.getProperty("line.separator"));
                while (st.hasMoreElements()) {
                    print.println(st.nextToken());
                }

                JOptionPane.showMessageDialog(rootPane, "Note Saved");
                fileName = fc.getSelectedFile().getName();
            }
        } catch (Exception e) {
        }
        finally{
            if(x == 0){
                print.close();
            }            
        }
    }//GEN-LAST:event_noteSaveAsActionPerformed

    private void clearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearActionPerformed
        noteBook.setText("");
    }//GEN-LAST:event_clearActionPerformed

    private void pointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pointActionPerformed
        if (expLength <= 35) {
            expression += ".";
            expressionField.setText(expression);
            expLength++;
        }

    }//GEN-LAST:event_pointActionPerformed

    private void zeroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zeroActionPerformed
        if (expLength <= 35) {
            expression += "0";
            expressionField.setText(expression);
            expLength++;
        }
    }//GEN-LAST:event_zeroActionPerformed

    private void equalsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_equalsActionPerformed
        calculate(expressionField.getText());
    }//GEN-LAST:event_equalsActionPerformed

    private void oneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_oneActionPerformed
        if (expLength <= 35) {
            expression += "1";
            expressionField.setText(expression);
            expLength++;
        }
    }//GEN-LAST:event_oneActionPerformed

    private void twoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_twoActionPerformed
        if (expLength <= 35) {
            expression += "2";
            expressionField.setText(expression);
            expLength++;
        }
    }//GEN-LAST:event_twoActionPerformed

    private void minusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minusActionPerformed
        if (expLength <= 35) {
            expression += "-";
            expressionField.setText(expression);
            expLength++;
        }
    }//GEN-LAST:event_minusActionPerformed

    private void fourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fourActionPerformed
        if (expLength <= 35) {
            expression += "4";
            expressionField.setText(expression);
            expLength++;
        }
    }//GEN-LAST:event_fourActionPerformed

    private void fiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fiveActionPerformed
        if (expLength <= 35) {
            expression += "5";
            expressionField.setText(expression);
            expLength++;
        }
    }//GEN-LAST:event_fiveActionPerformed

    private void sixActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sixActionPerformed
        if (expLength <= 35) {
            expression += "6";
            expressionField.setText(expression);
            expLength++;
        }
    }//GEN-LAST:event_sixActionPerformed

    private void multiplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multiplyActionPerformed
        if (expLength <= 35) {
            expression += "*";
            expressionField.setText(expression);
            expLength++;
        }
    }//GEN-LAST:event_multiplyActionPerformed

    private void powerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_powerActionPerformed
        if (expLength <= 35) {
            expression += "^";
            expressionField.setText(expression);
            expLength++;
        }
    }//GEN-LAST:event_powerActionPerformed

    private void sevenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sevenActionPerformed
        if (expLength <= 35) {
            expression += "7";
            expressionField.setText(expression);
            expLength++;
        }
    }//GEN-LAST:event_sevenActionPerformed

    private void eightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eightActionPerformed
        if (expLength <= 35) {
            expression += "8";
            expressionField.setText(expression);
            expLength++;
        }
    }//GEN-LAST:event_eightActionPerformed

    private void nineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nineActionPerformed
        if (expLength <= 35) {
            expression += "9";
            expressionField.setText(expression);
            expLength++;
        }
    }//GEN-LAST:event_nineActionPerformed

    private void divideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_divideActionPerformed
        if (expLength <= 35) {
            expression += "/";
            expressionField.setText(expression);
            expLength++;
        }
    }//GEN-LAST:event_divideActionPerformed

    private void clearExpressionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearExpressionActionPerformed
        expression = "";
        expressionField.setText("");
        expLength = 0;
        RES.setText("");
    }//GEN-LAST:event_clearExpressionActionPerformed

    private void expressionFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_expressionFieldKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_expressionFieldKeyTyped

    private void expressionFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expressionFieldActionPerformed

    }//GEN-LAST:event_expressionFieldActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        CGPA();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void CGPAcalculateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CGPAcalculateActionPerformed
        System.out.println(Count);
        if(Count == 0){
            JOptionPane.showMessageDialog(rootPane, "No input!");
        }else{
            CGPAcal();
        }
        
        CGPAclean();
    }//GEN-LAST:event_CGPAcalculateActionPerformed

    private void cCleanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cCleanActionPerformed
        CGPAclean();
    }//GEN-LAST:event_cCleanActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        JOptionPane.showMessageDialog(rootPane, "theOne: Personal Assistant.");
    }//GEN-LAST:event_jButton10ActionPerformed

    public void CGPAclean(){
        credits = (float)0.0;
        results = (float)0.0;
        cCount.setText("1");
        cInput.setText("0");
        Count = 0;
        credit.setSelectedItem("1");
        result.setSelectedItem("2.00");
    }
        
    
    public static void main(String args[]) {
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
            java.util.logging.Logger.getLogger(MotherContainerFinal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MotherContainerFinal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MotherContainerFinal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MotherContainerFinal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MotherContainerFinal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CGPAcalculate;
    private javax.swing.JLabel RES;
    private javax.swing.JTable TaskOverViewTable;
    private javax.swing.JButton addTask;
    private javax.swing.JButton addTask1;
    private javax.swing.JButton addTask2;
    private javax.swing.JButton answer;
    private javax.swing.JLabel assign;
    private javax.swing.JButton backSpace;
    private javax.swing.JTextField bhS;
    private javax.swing.JTextField bmS;
    private javax.swing.JComboBox btS;
    private javax.swing.JButton cClean;
    private javax.swing.JLabel cCount;
    private javax.swing.JLabel cInput;
    private javax.swing.JPanel calButtonPanel;
    private javax.swing.JButton clear;
    private javax.swing.JButton clearExpression;
    private javax.swing.JButton clearFields;
    private javax.swing.JLabel createLabel;
    private javax.swing.JComboBox credit;
    private javax.swing.JButton deleteTask;
    private javax.swing.JButton divide;
    private javax.swing.JTextField ehS;
    private javax.swing.JButton eight;
    private javax.swing.JTextField emS;
    private javax.swing.JButton equals;
    private javax.swing.JComboBox etS;
    private javax.swing.JTextField expressionField;
    private javax.swing.JButton five;
    private javax.swing.JButton four;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton minus;
    private javax.swing.JButton multiply;
    private javax.swing.JButton nine;
    private javax.swing.JTextArea noteBook;
    private javax.swing.JButton noteSave;
    private javax.swing.JButton noteSaveAs;
    private javax.swing.JButton one;
    private javax.swing.JButton open;
    private javax.swing.JButton plus;
    private javax.swing.JButton point;
    private javax.swing.JButton power;
    private javax.swing.JComboBox result;
    private javax.swing.JButton seven;
    private javax.swing.JButton six;
    private javax.swing.JButton sqrt;
    private javax.swing.JPanel stopWatchPanel;
    private javax.swing.JLabel swLabel;
    private javax.swing.JButton swPause;
    private javax.swing.JButton swReset;
    private javax.swing.JButton swStart;
    private javax.swing.JPanel taskPanel;
    private javax.swing.JTextField taskPicker;
    private javax.swing.JButton three;
    private javax.swing.JLabel to;
    private javax.swing.JButton two;
    private javax.swing.JButton zero;
    // End of variables declaration//GEN-END:variables
}
