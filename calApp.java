package calculatorApp;
import java.awt.Color;
import javax.swing.*;
// Better: just use
// Better: just use

/**
 *
 * @author hp
 */
public class calApp extends JFrame {
    
    //private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(calApp.class.getName());
// This stores whatever is currently shown on the display
private String currentExpression = "";

// This is for memory functions (MC, MR, MS, M+, M-)
private double memoryValue = 0;
    /**
     * Creates new form calApp
     */
    public calApp() {
        initComponents();
        setTitle("Calculator");
        setLocationRelativeTo(null);
         getContentPane().setBackground(Color.cyan);
         setupButtonListeners(); // <-- we call our new setup method here
    }
// Called for buttons: 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, and "."
private void appendToExpression(String value) {
    currentExpression += value;         // Add the digit to the expression string
    jTextField1.setText(currentExpression); // Show the updated expression on display
}
private void appendOperator(String op) {
    // Prevent double operators: if expression already ends with an operator, replace it
    if (!currentExpression.isEmpty()) {
        char last = currentExpression.charAt(currentExpression.length() - 1);
        if ("+-*/%^".indexOf(last) >= 0) {//Checks if the last character is one of those operator
            currentExpression = currentExpression.substring(0, currentExpression.length() - 1);
        }
    }//symbols. indexOf returns -1 if not found, so >= 0 means "it IS an operator."
//Then it appends the new operator and updates the display.
    currentExpression += op;
    jTextField1.setText(currentExpression);
}
// ==================== EXPRESSION EVALUATOR (No ScriptEngine needed) ====================

private double evalMath(String expr) {
    expr = expr.trim();
    // Handle ^ (power)
    expr = expr.replace("^", " POW ");

    // Use a simple recursive parser
    return parseExpr(expr, new int[]{0});
}

private double parseExpr(String expr, int[] pos) {
    double result = parseTerm(expr, pos);
    OUTER:
    while (pos[0] < expr.length()) {
        char op = expr.charAt(pos[0]);
        switch (op) {
            case '+' -> {
                pos[0]++;
                result += parseTerm(expr, pos);
            }
            case '-' -> {
                pos[0]++;
                result -= parseTerm(expr, pos);
            }
            default -> {
                break OUTER;
            }
        }
    }
    return result;
}

private double parseTerm(String expr, int[] pos) {
    double result = parsePower(expr, pos);
    OUTER:
    while (pos[0] < expr.length()) {
        char op = expr.charAt(pos[0]);
        switch (op) {
            case '*' -> {
                pos[0]++;
                result *= parsePower(expr, pos);
            }
            case '/' -> {
                pos[0]++;
                result /= parsePower(expr, pos);
            }
            case '%' -> {
                pos[0]++;
                result %= parsePower(expr, pos);
            }
            default -> {
                break OUTER;
            }
        }
    }
    return result;
}

private double parsePower(String expr, int[] pos) {
    // Check for POW keyword
    double base = parseFactor(expr, pos);
    skipSpaces(expr, pos);
    if (pos[0] + 3 <= expr.length() && expr.substring(pos[0], pos[0] + 3).equals("POW")) {
        pos[0] += 3;
        double exp = parseFactor(expr, pos);
        return Math.pow(base, exp);
    }
    return base;
}

private double parseFactor(String expr, int[] pos) {
    skipSpaces(expr, pos);
    if (pos[0] >= expr.length()) throw new RuntimeException("Unexpected end");
    // Handle unary minus
    if (expr.charAt(pos[0]) == '-') {
        pos[0]++;
        return -parseFactor(expr, pos);
    }
    // Handle parentheses
    if (expr.charAt(pos[0]) == '(') {
        pos[0]++; // skip '('
        double val = parseExpr(expr, pos);
        skipSpaces(expr, pos);
        if (pos[0] < expr.length() && expr.charAt(pos[0]) == ')') pos[0]++;
        return val;
    }
    // Parse number
    int start = pos[0];
    if (expr.charAt(pos[0]) == '+') pos[0]++; // skip unary plus
    while (pos[0] < expr.length() && (Character.isDigit(expr.charAt(pos[0]))
            || expr.charAt(pos[0]) == '.' || expr.charAt(pos[0]) == 'E'
            || (expr.charAt(pos[0]) == '-' && pos[0] > 0 && expr.charAt(pos[0]-1) == 'E'))) {
        pos[0]++;
    }
    skipSpaces(expr, pos);
    return Double.parseDouble(expr.substring(start, pos[0]).trim());
}

private void skipSpaces(String expr, int[] pos) {
    while (pos[0] < expr.length() && expr.charAt(pos[0]) == ' ') pos[0]++;
}

// ==================== UPDATED evaluateExpression ====================
private void evaluateExpression() {
    try {
        double result = evalMath(currentExpression);
        // Show as integer if no decimal part
        if (result == Math.floor(result) && !Double.isInfinite(result)) {
            currentExpression = String.valueOf((long) result);
        } else {
            currentExpression = String.valueOf(result);
        }
        jTextField1.setText(currentExpression);
    } catch (Exception e) {
        jTextField1.setText("Error");
        currentExpression = "";
    }
}

// ==================== UPDATED reciprocal ====================
private void reciprocal() {
    try {
        double val = evalMath(currentExpression);
        currentExpression = String.valueOf(1.0 / val);
        jTextField1.setText(currentExpression);
    } catch (Exception e) {
        jTextField1.setText("Error");
        currentExpression = "";
    }
}

// C — clears everything
private void clearAll() {
    currentExpression = "";
    jTextField1.setText("");
}

// CE — clears the last number entered (not operators), useful for fixing mistakes
private void clearEntry() {
    // Find last operator and remove everything after it
    int lastOp = -1;
    for (int i = currentExpression.length() - 1; i >= 0; i--) {
        char c = currentExpression.charAt(i);
        if ("+-*/%^".indexOf(c) >= 0) { lastOp = i; break; }
    }
    currentExpression = (lastOp >= 0) ? currentExpression.substring(0, lastOp + 1) : "";
    jTextField1.setText(currentExpression);
}

// Del — removes just the last character (like a backspace)
private void deleteLast() {
    if (!currentExpression.isEmpty()) {
        currentExpression = currentExpression.substring(0, currentExpression.length() - 1);
        jTextField1.setText(currentExpression);
    }
}
// MC — Memory Clear: wipe the stored memory value
private void memoryClear() { memoryValue = 0; }

// MR — Memory Recall: paste stored value into expression
private void memoryRecall() {
    currentExpression += memoryValue;
    jTextField1.setText(currentExpression);
}

// MS — Memory Store: save current display value into memory
private void memoryStore() {
    try { memoryValue = Double.parseDouble(currentExpression); } catch (NumberFormatException e) {}
}

// M+ — Add current display value TO memory
private void memoryAdd() {
    try { memoryValue += Double.parseDouble(currentExpression); } catch (NumberFormatException e) {}
}

// M- — Subtract current display value FROM memory
private void memorySubtract() {
    try { memoryValue -= Double.parseDouble(currentExpression); } catch (NumberFormatException e) {}
}

// EXP — Scientific notation: appends "E" for entering numbers like 1.5E3 = 1500
private void appendExp() {
    currentExpression += "E";
    jTextField1.setText(currentExpression);
}
//wiring everything
private void setupButtonListeners() {
    //This is the wiring — connecting every button to its function using lambda expressions e->
    // ==================== NUMBER BUTTONS ====================
    btnZERO.addActionListener(e -> appendToExpression("0"));
    btnONE.addActionListener(e -> appendToExpression("1"));
    btnTWO.addActionListener(e -> appendToExpression("2"));
    btnTHREE.addActionListener(e -> appendToExpression("3"));
    btnFOUR.addActionListener(e -> appendToExpression("4"));
    btnFIVE.addActionListener(e -> appendToExpression("5"));
    btnSIX.addActionListener(e -> appendToExpression("6"));
    btnSEVEN.addActionListener(e -> appendToExpression("7"));
    btnEIGHT.addActionListener(e -> appendToExpression("8"));
    btnNINE.addActionListener(e -> appendToExpression("9"));
    
    btnPOINT.addActionListener(e -> appendToExpression("."));
    
    // ==================== OPERATORS ====================
    btnADD.addActionListener(e -> appendOperator("+"));
    btnSUB.addActionListener(e -> appendOperator("-"));
    btnMULTIPLY.addActionListener(e -> appendOperator("*"));
    btnDIV.addActionListener(e -> appendOperator("/"));
    btnPOWER.addActionListener(e -> appendOperator("^"));
    
    // ==================== SPECIAL FUNCTIONS ====================
    btnEQUALS.addActionListener(e -> evaluateExpression());
    
    btnC.addActionListener(e -> clearAll());
    btnCE.addActionListener(e -> clearEntry());
    btnDel.addActionListener(e -> deleteLast());
    
    // Memory functions
    btnMC.addActionListener(e -> memoryClear());
    btnMR.addActionListener(e -> memoryRecall());
    btnMS.addActionListener(e -> memoryStore());
    btnM_add.addActionListener(e -> memoryAdd());
    btnM_sub.addActionListener(e -> memorySubtract());
    
    // Other functions
    btnDECIMAL.addActionListener(e -> reciprocal());   // 1/X button
    jButton34.addActionListener(e -> appendExp());     // EXP button
    
    // Percentage (you can improve this later)
    btnPERCENTAGE.addActionListener(e -> {
        try {
            if (!currentExpression.isEmpty()) {
                double val = Double.parseDouble(currentExpression);
                currentExpression = String.valueOf(val / 100);
                jTextField1.setText(currentExpression);
            }
        } catch (NumberFormatException ex) {
            jTextField1.setText("Error");
            currentExpression = "";
        }
    });
}
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenu5 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jButton8 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        btnMC = new javax.swing.JButton();
        btnDel = new javax.swing.JButton();
        btnSEVEN = new javax.swing.JButton();
        btnFOUR = new javax.swing.JButton();
        btnTHREE = new javax.swing.JButton();
        btnZERO = new javax.swing.JButton();
        btnMR = new javax.swing.JButton();
        btnCE = new javax.swing.JButton();
        btnMS = new javax.swing.JButton();
        btnM_add = new javax.swing.JButton();
        btnM_sub = new javax.swing.JButton();
        btnC = new javax.swing.JButton();
        btnADD = new javax.swing.JButton();
        btnDIV = new javax.swing.JButton();
        btnEIGHT = new javax.swing.JButton();
        btnNINE = new javax.swing.JButton();
        btnMULTIPLY = new javax.swing.JButton();
        btnPERCENTAGE = new javax.swing.JButton();
        btnFIVE = new javax.swing.JButton();
        btnSIX = new javax.swing.JButton();
        btnSUB = new javax.swing.JButton();
        btnDECIMAL = new javax.swing.JButton();
        btnTWO = new javax.swing.JButton();
        btnPOINT = new javax.swing.JButton();
        btnPOWER = new javax.swing.JButton();
        jButton34 = new javax.swing.JButton();
        btnEQUALS = new javax.swing.JButton();
        btnONE = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        jMenu4 = new javax.swing.JMenu();
        jMenu6 = new javax.swing.JMenu();
        jMenu7 = new javax.swing.JMenu();

        jMenu1.setText("jMenu1");

        jMenu2.setText("jMenu2");

        jMenu5.setText("jMenu5");

        jMenuItem1.setText("jMenuItem1");

        jButton8.setText("jButton8");

        jButton18.setText("jButton18");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnMC.setText("MC");

        btnDel.setText("Del");

        btnSEVEN.setText("7");

        btnFOUR.setText("4");

        btnTHREE.setText("3");

        btnZERO.setText("0");

        btnMR.setText("MR");

        btnCE.setText("CE");

        btnMS.setText("MS");

        btnM_add.setText("M+");

        btnM_sub.setText("M-");

        btnC.setText("C");

        btnADD.setText("+");

        btnDIV.setText("/");

        btnEIGHT.setText("8");

        btnNINE.setText("9");

        btnMULTIPLY.setText("*");

        btnPERCENTAGE.setText("%");

        btnFIVE.setText("5");
        btnFIVE.addActionListener(this::btnFIVEActionPerformed);

        btnSIX.setText("6");

        btnSUB.setText("-");

        btnDECIMAL.setText("1/X");

        btnTWO.setText("2");

        btnPOINT.setText(".");

        btnPOWER.setText("^");

        jButton34.setText("EXP");

        btnEQUALS.setText("=");

        btnONE.setText("1");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(btnSEVEN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnDel, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                .addComponent(btnMC, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btnFOUR, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnTHREE, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnCE, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnEIGHT, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnMR, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnFIVE, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnTWO, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnONE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnNINE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnMS, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnSIX, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(btnZERO, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnM_add, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnADD, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnMULTIPLY, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSUB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnPOINT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton34, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnM_sub, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDIV, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnDECIMAL, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                    .addComponent(btnPOWER, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnEQUALS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnPERCENTAGE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnMC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnMS, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnM_add)
                                .addComponent(btnM_sub))
                            .addComponent(btnMR, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnDel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnCE)
                            .addComponent(btnC)
                            .addComponent(btnADD)
                            .addComponent(btnDIV))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnSEVEN)
                                .addComponent(btnEIGHT))
                            .addComponent(btnNINE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnPERCENTAGE)
                            .addComponent(btnMULTIPLY))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnFOUR, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnFIVE)
                        .addComponent(btnSUB)
                        .addComponent(btnDECIMAL)
                        .addComponent(btnSIX)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTHREE)
                    .addComponent(btnTWO)
                    .addComponent(btnPOINT)
                    .addComponent(btnPOWER)
                    .addComponent(btnONE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton34)
                        .addComponent(btnEQUALS))
                    .addComponent(btnZERO))
                .addGap(14, 14, 14))
        );

        jMenuBar1.add(jMenu3);

        jMenu4.setText("View");
        jMenuBar1.add(jMenu4);

        jMenu6.setText("Edit");
        jMenuBar1.add(jMenu6);

        jMenu7.setText("Help");
        jMenuBar1.add(jMenu7);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jTextField1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnFIVEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFIVEActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnFIVEActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
//            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
      calApp calapp= new calApp();
      calapp.setVisible(true);

        /* Create and display the form */
        //java.awt.EventQueue.invokeLater(() -> new calApp().setVisible(true));
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnADD;
    private javax.swing.JButton btnC;
    private javax.swing.JButton btnCE;
    private javax.swing.JButton btnDECIMAL;
    private javax.swing.JButton btnDIV;
    private javax.swing.JButton btnDel;
    private javax.swing.JButton btnEIGHT;
    private javax.swing.JButton btnEQUALS;
    private javax.swing.JButton btnFIVE;
    private javax.swing.JButton btnFOUR;
    private javax.swing.JButton btnMC;
    private javax.swing.JButton btnMR;
    private javax.swing.JButton btnMS;
    private javax.swing.JButton btnMULTIPLY;
    private javax.swing.JButton btnM_add;
    private javax.swing.JButton btnM_sub;
    private javax.swing.JButton btnNINE;
    private javax.swing.JButton btnONE;
    private javax.swing.JButton btnPERCENTAGE;
    private javax.swing.JButton btnPOINT;
    private javax.swing.JButton btnPOWER;
    private javax.swing.JButton btnSEVEN;
    private javax.swing.JButton btnSIX;
    private javax.swing.JButton btnSUB;
    private javax.swing.JButton btnTHREE;
    private javax.swing.JButton btnTWO;
    private javax.swing.JButton btnZERO;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton34;
    private javax.swing.JButton jButton8;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
