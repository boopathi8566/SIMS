package esec.sims;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.List;

public class Main {
    static final Color ORANGE = new Color(37, 99, 235);
    static final Color LIGHT = new Color(246, 248, 252);
    static final Color BLUE = new Color(30, 64, 175);
    static final Color NAVY = new Color(15, 23, 42);
    static final Color MUTED = new Color(100, 116, 139);
    static final DecimalFormat DF = new DecimalFormat("0.0");

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new LoginFrame();
        });
    }

    static void msg(Component c, String s) { JOptionPane.showMessageDialog(c, s); }
    static boolean confirm(Component c, String s) {
        return JOptionPane.showConfirmDialog(c, s, "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setForeground(NAVY);
        return l;
    }

    static JButton btn(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setBackground(ORANGE);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(10,18,10,18));
        return b;
    }

    static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226,232,240)),
                new EmptyBorder(16,16,16,16)));
        return p;
    }

    static void styleField(JComponent c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203,213,225)),
                new EmptyBorder(8,10,8,10)));
        c.setBackground(Color.WHITE);
    }

    // ---------------- LOGIN ----------------
    static class LoginFrame extends JFrame {
        JTextField user = new JTextField();
        JPasswordField pass = new JPasswordField();
        JComboBox<String> role = new JComboBox<>(new String[]{"Student", "Staff", "Administrator"});

        LoginFrame() {
            setTitle("ESEC SIMS | Secure Login");
            setSize(920, 560);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setResizable(true);
            setMinimumSize(new Dimension(920, 560));

            JPanel root = new JPanel(new GridLayout(1, 2));
            root.setBackground(LIGHT);

            JPanel brand = new JPanel();
            brand.setBackground(NAVY);
            brand.setBorder(new EmptyBorder(55, 50, 55, 50));
            brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));

            JLabel mark = new JLabel("ESEC");
            mark.setFont(new Font("Segoe UI", Font.BOLD, 34));
            mark.setForeground(Color.WHITE);
            mark.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel system = new JLabel("Student Information");
            system.setText("Student Information");
            system.setFont(new Font("Segoe UI", Font.BOLD, 28));
            system.setForeground(Color.WHITE);
            system.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel system2 = new JLabel("Management System");
            system2.setFont(new Font("Segoe UI", Font.BOLD, 28));
            system2.setForeground(new Color(147,197,253));
            system2.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel line = new JLabel("Centralized academic management • Secure role-based access");
            line.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            line.setForeground(new Color(203,213,225));
            line.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel college = new JLabel("Erode Sengunthar Engineering College");
            college.setFont(new Font("Segoe UI", Font.BOLD, 13));
            college.setForeground(new Color(148,163,184));
            college.setAlignmentX(Component.LEFT_ALIGNMENT);

            brand.add(mark); brand.add(Box.createVerticalStrut(70));
            brand.add(system); brand.add(system2);
            brand.add(Box.createVerticalStrut(18)); brand.add(line);
            brand.add(Box.createVerticalGlue()); brand.add(college);

            JPanel right = new JPanel(new BorderLayout());
            right.setBackground(LIGHT);
            right.setBorder(new EmptyBorder(55,65,45,65));

            JPanel form = new JPanel();
            form.setOpaque(false);
            form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

            JLabel welcome = new JLabel("Welcome back");
            welcome.setFont(new Font("Segoe UI", Font.BOLD, 30));
            welcome.setForeground(NAVY);

            JLabel sub = new JLabel("Sign in to continue to SIMS");
            sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            sub.setForeground(MUTED);

            JLabel roleLabel = new JLabel("LOGIN AS");
            roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            roleLabel.setForeground(MUTED);

            JLabel userLabel = new JLabel("ID / ROLL NUMBER");
            userLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            userLabel.setForeground(MUTED);

            JLabel passLabel = new JLabel("PASSWORD / REGISTERED MOBILE");
            passLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            passLabel.setForeground(MUTED);

            styleField(role); styleField(user); styleField(pass);
            role.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            user.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            pass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

            // Password visibility toggle
            JCheckBox showPassword = new JCheckBox("Show password");
            showPassword.setOpaque(false);
            showPassword.setFocusPainted(false);
            showPassword.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            showPassword.setForeground(MUTED);
            showPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
            showPassword.addActionListener(e -> {
                pass.setEchoChar(showPassword.isSelected() ? (char) 0 : '•');
            });
            pass.setEchoChar('•');

            JButton login = btn("SIGN IN  →");
            login.setAlignmentX(Component.LEFT_ALIGNMENT);
            login.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            login.addActionListener(e -> login());

            form.add(welcome); form.add(Box.createVerticalStrut(5));
            form.add(sub); form.add(Box.createVerticalStrut(30));
            form.add(roleLabel); form.add(Box.createVerticalStrut(6)); form.add(role);
            form.add(Box.createVerticalStrut(18)); form.add(userLabel); form.add(Box.createVerticalStrut(6)); form.add(user);
            form.add(Box.createVerticalStrut(18)); form.add(passLabel); form.add(Box.createVerticalStrut(6)); form.add(pass);
            form.add(Box.createVerticalStrut(6)); form.add(showPassword);
            form.add(Box.createVerticalStrut(22)); form.add(login);

            JLabel hint = new JLabel("Student: Roll No + Mobile   |   Staff/Admin: ID + Password");
            hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            hint.setForeground(MUTED);
            hint.setBorder(new EmptyBorder(18,0,0,0));

            JPanel center = new JPanel(new BorderLayout());
            center.setOpaque(false); center.add(form, BorderLayout.NORTH);
            right.add(center, BorderLayout.CENTER); right.add(hint, BorderLayout.SOUTH);

            root.add(brand); root.add(right);
            setContentPane(root);
            setVisible(true);
        }

        void login() {
            String r = (String) role.getSelectedItem();
            String u = user.getText().trim();
            String p = new String(pass.getPassword()).trim();

            if (u.isEmpty() || p.isEmpty()) {
                msg(this, "Enter all login details.");
                return;
            }

            try (Connection c = DB.getConnection()) {
                if ("Administrator".equals(r)) {
                    String sql = "SELECT admin_id, name FROM administrators WHERE admin_id=? AND password=? AND active=1";
                    try (PreparedStatement ps = DB.prepare(c, sql, u, p); ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            dispose();
                            new AdminFrame(rs.getString("admin_id"), rs.getString("name"));
                        } else msg(this, "Invalid administrator ID or password.");
                    }
                } else if ("Staff".equals(r)) {
                    String sql = "SELECT staff_id, name FROM staff WHERE staff_id=? AND password=? AND active=1";
                    try (PreparedStatement ps = DB.prepare(c, sql, u, p); ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            dispose();
                            new StaffFrame(rs.getString("staff_id"), rs.getString("name"));
                        } else msg(this, "Invalid staff ID or password.");
                    }
                } else {
                    String sql = "SELECT roll_no, name FROM students WHERE roll_no=? AND mobile=?";
                    try (PreparedStatement ps = DB.prepare(c, sql, u, p); ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            dispose();
                            new StudentFrame(rs.getString("roll_no"), rs.getString("name"));
                        } else msg(this, "Invalid roll number or registered mobile.");
                    }
                }
            } catch (SQLException ex) {
                msg(this, "Database connection failed.\n" + ex.getMessage() + "\n\nCheck MySQL is running and DB.java credentials are correct.");
            }
        }
    }

    // ---------------- ADMINISTRATOR ----------------
    static class AdminFrame extends JFrame {
        final String adminId;
        final DefaultTableModel staffModel = new DefaultTableModel(new String[]{"ID","Name","Active"},0) {
            public boolean isCellEditable(int r,int c){return false;}
        };
        final JTable staffTable = new JTable(staffModel);

        AdminFrame(String adminId, String name) {
            this.adminId=adminId;
            setTitle("ESEC SIMS | Administrator"); setSize(1000,650);
            setLocationRelativeTo(null); setDefaultCloseOperation(EXIT_ON_CLOSE);
            JPanel top=new JPanel(new BorderLayout()); top.setBackground(Color.WHITE);
            top.setBorder(new EmptyBorder(16,20,16,20));
            top.add(title("Administrator Console"),BorderLayout.WEST);
            top.add(new JLabel("Signed in: "+name+" ("+adminId+")"),BorderLayout.EAST);

            JPanel content=new JPanel(new BorderLayout(15,15)); content.setBackground(LIGHT);
            content.setBorder(new EmptyBorder(18,18,18,18));
            JPanel form=card(); form.setLayout(new GridLayout(2,4,10,8));
            JTextField id=new JTextField(), nm=new JTextField(); JPasswordField pw=new JPasswordField();
            JButton add=btn("ADD STAFF");
            form.add(new JLabel("Staff ID")); form.add(new JLabel("Name")); form.add(new JLabel("Password")); form.add(new JLabel(""));
            form.add(id); form.add(nm); form.add(pw); form.add(add);
            add.addActionListener(e->{
                try{ DB.update("INSERT INTO staff(staff_id,name,password,active) VALUES(?,?,?,1)",id.getText().trim(),nm.getText().trim(),new String(pw.getPassword()));
                    loadStaff(); msg(this,"Staff account created."); id.setText("");nm.setText("");pw.setText("");
                }catch(Exception ex){msg(this,"Could not create account: "+ex.getMessage());}
            });
            JButton toggle=btn("TOGGLE ACTIVE");
            toggle.addActionListener(e->{int row=staffTable.getSelectedRow(); if(row<0){msg(this,"Select a staff account.");return;} String sid=staffModel.getValueAt(row,0).toString(); boolean active="YES".equals(staffModel.getValueAt(row,2)); try{DB.update("UPDATE staff SET active=? WHERE staff_id=?",!active,sid);loadStaff();}catch(Exception ex){msg(this,ex.getMessage());}});
            JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT)); bottom.setOpaque(false); bottom.add(toggle);
            content.add(form,BorderLayout.NORTH); content.add(new JScrollPane(staffTable),BorderLayout.CENTER); content.add(bottom,BorderLayout.SOUTH);
            JButton logout=btn("Logout"); logout.addActionListener(e->{dispose();new LoginFrame();});
            JPanel foot=new JPanel(new FlowLayout(FlowLayout.RIGHT)); foot.add(logout);
            add(top,BorderLayout.NORTH);add(content,BorderLayout.CENTER);add(foot,BorderLayout.SOUTH);
            loadStaff(); setVisible(true);
        }
        void loadStaff(){ staffModel.setRowCount(0); try(Connection c=DB.getConnection();PreparedStatement ps=c.prepareStatement("SELECT staff_id,name,active FROM staff ORDER BY staff_id");ResultSet rs=ps.executeQuery()){while(rs.next())staffModel.addRow(new Object[]{rs.getString(1),rs.getString(2),rs.getBoolean(3)?"YES":"NO"});}catch(Exception ex){msg(this,"Staff load error: "+ex.getMessage());}}
    }

    // ---------------- STAFF ----------------
    static class StaffFrame extends JFrame {
        final String staffId;
        final JTabbedPane tabs = new JTabbedPane();

        StaffFrame(String staffId, String name) {
            this.staffId = staffId;
            setTitle("ESEC SIMS - Staff Dashboard");
            setSize(1200, 760);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(EXIT_ON_CLOSE);

            JPanel top = new JPanel(new BorderLayout());
            top.setBackground(LIGHT);
            top.setBorder(new EmptyBorder(10,15,10,15));
            top.add(title("Staff Dashboard"), BorderLayout.WEST);
            JLabel who = new JLabel("Logged in: " + name + " (" + staffId + ")");
            who.setFont(new Font("Segoe UI", Font.BOLD, 13));
            top.add(who, BorderLayout.EAST);

            tabs.addTab("Students", studentsPanel());
            tabs.addTab("Subjects", subjectsPanel());
            tabs.addTab("Marks", marksPanel());
            tabs.addTab("Attendance", attendancePanel());
            tabs.addTab("Profile Permission", permissionPanel());

            JButton logout = btn("Logout");
            logout.addActionListener(e -> { dispose(); new LoginFrame(); });
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(logout);

            add(top, BorderLayout.NORTH);
            add(tabs, BorderLayout.CENTER);
            add(bottom, BorderLayout.SOUTH);
            setVisible(true);
        }

        JPanel studentsPanel() {
            JPanel p = new JPanel(new BorderLayout(10,10));
            p.setBorder(new EmptyBorder(12,12,12,12));
            String[] cols = {"Roll No","Name","Department","Year","Mobile","Email","Edit Enabled"};
            DefaultTableModel m = new DefaultTableModel(cols,0) {
                public boolean isCellEditable(int r,int c){return false;}
            };
            JTable table = new JTable(m);
            refreshStudents(m);

            JPanel form = card();
            form.setLayout(new GridLayout(2,7,6,6));
            JTextField roll=new JTextField(), name=new JTextField(), dept=new JTextField("CSE-A"),
                    year=new JTextField("2"), mobile=new JTextField(), email=new JTextField();
            JButton add=btn("Add Student");
            form.add(new JLabel("Roll No")); form.add(new JLabel("Name")); form.add(new JLabel("Dept"));
            form.add(new JLabel("Year")); form.add(new JLabel("Mobile")); form.add(new JLabel("Email")); form.add(new JLabel(""));
            form.add(roll); form.add(name); form.add(dept); form.add(year); form.add(mobile); form.add(email); form.add(add);

            add.addActionListener(e -> {
                if (roll.getText().isBlank() || name.getText().isBlank()) {
                    msg(this, "Roll number and name are required."); return;
                }
                try {
                    DB.update("INSERT INTO students(roll_no,name,department,year,mobile,email,edit_permission) VALUES(?,?,?,?,?,?,0)",
                            roll.getText().trim().toUpperCase(),name.getText().trim(),dept.getText().trim(),
                            Integer.parseInt(year.getText().trim()),mobile.getText().trim(),email.getText().trim());
                    msg(this,"Student added.");
                    refreshStudents(m);
                    Arrays.asList(roll,name,mobile,email).forEach(x->x.setText(""));
                } catch(Exception ex){msg(this,"Could not add student: "+ex.getMessage());}
            });

            JButton refresh=btn("Refresh");
            refresh.addActionListener(e->refreshStudents(m));
            JPanel north=new JPanel(new BorderLayout());
            north.add(form,BorderLayout.CENTER);
            north.add(refresh,BorderLayout.SOUTH);
            p.add(north,BorderLayout.NORTH);
            p.add(new JScrollPane(table),BorderLayout.CENTER);
            return p;
        }

        void refreshStudents(DefaultTableModel m) {
            m.setRowCount(0);
            try(Connection c=DB.getConnection();
                PreparedStatement ps=c.prepareStatement("SELECT roll_no,name,department,year,mobile,email,edit_permission FROM students ORDER BY roll_no");
                ResultSet rs=ps.executeQuery()) {
                while(rs.next()) m.addRow(new Object[]{
                        rs.getString(1),rs.getString(2),rs.getString(3),rs.getInt(4),
                        rs.getString(5),rs.getString(6),rs.getBoolean(7)?"YES":"NO"});
            } catch(SQLException ex){msg(this,"Load error: "+ex.getMessage());}
        }

        JPanel subjectsPanel() {
            JPanel p=new JPanel(new BorderLayout(10,10));
            p.setBorder(new EmptyBorder(12,12,12,12));
            String[] cols={"ID","Semester","Code","Subject","Type","Credits"};
            DefaultTableModel m=new DefaultTableModel(cols,0){
                public boolean isCellEditable(int r,int c){return false;}
            };
            JTable t=new JTable(m); refreshSubjects(m);

            JPanel f=card(); f.setLayout(new GridLayout(2,7,6,6));
            JTextField sem=new JTextField("3"),code=new JTextField(),sub=new JTextField(),
                    type=new JTextField("Theory"),credits=new JTextField("3.0");
            JButton add=btn("Add Subject");
            f.add(new JLabel("Semester"));f.add(new JLabel("Code"));f.add(new JLabel("Subject"));
            f.add(new JLabel("Type"));f.add(new JLabel("Credits"));f.add(new JLabel(""));f.add(new JLabel(""));
            f.add(sem);f.add(code);f.add(sub);f.add(type);f.add(credits);f.add(add);f.add(new JLabel(""));
            add.addActionListener(e->{
                try{
                    DB.update("INSERT INTO subjects(semester,code,name,type,credits) VALUES(?,?,?,?,?)",
                            Integer.parseInt(sem.getText()),code.getText().trim(),sub.getText().trim(),
                            type.getText().trim(),Double.parseDouble(credits.getText()));
                    refreshSubjects(m); msg(this,"Subject added.");
                }catch(Exception ex){msg(this,"Could not add subject: "+ex.getMessage());}
            });
            p.add(f,BorderLayout.NORTH);p.add(new JScrollPane(t),BorderLayout.CENTER);
            return p;
        }

        void refreshSubjects(DefaultTableModel m){
            m.setRowCount(0);
            try(Connection c=DB.getConnection();
                PreparedStatement ps=c.prepareStatement("SELECT id,semester,code,name,type,credits FROM subjects ORDER BY semester,code");
                ResultSet rs=ps.executeQuery()){
                while(rs.next())m.addRow(new Object[]{rs.getInt(1),rs.getInt(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getDouble(6)});
            }catch(SQLException ex){msg(this,"Load error: "+ex.getMessage());}
        }

        JPanel marksPanel() {
            JPanel p=new JPanel(new BorderLayout(10,10));
            p.setBorder(new EmptyBorder(12,12,12,12));

            JPanel form=card();
            form.setLayout(new GridLayout(5,5,8,8));
            JTextField roll=new JTextField();
            JComboBox<String> subjectBox=new JComboBox<>();
            JTextField cat1=new JTextField(),cat2=new JTextField(),
                    sac1=new JTextField(),sac2=new JTextField(),sac3=new JTextField(),
                    sac4=new JTextField(),sac5=new JTextField();
            JButton load=btn("LOAD MARKS");
            JButton save=btn("SAVE / UPDATE MARKS");

            try(Connection c=DB.getConnection();
                PreparedStatement ps=c.prepareStatement("SELECT code,name FROM subjects WHERE semester=3 ORDER BY code");
                ResultSet rs=ps.executeQuery()){
                while(rs.next()) subjectBox.addItem(rs.getString("code")+" - "+rs.getString("name"));
            }catch(SQLException ex){msg(this,"Subject loading error: "+ex.getMessage());}

            form.add(new JLabel("Student Roll No")); form.add(roll);
            form.add(new JLabel("Subject")); form.add(subjectBox); form.add(load);
            form.add(new JLabel("CAT I /100")); form.add(cat1);
            form.add(new JLabel("CAT II /100")); form.add(cat2); form.add(new JLabel(""));
            form.add(new JLabel("SAC I /20")); form.add(sac1);
            form.add(new JLabel("SAC II /20")); form.add(sac2); form.add(new JLabel(""));
            form.add(new JLabel("SAC III /20")); form.add(sac3);
            form.add(new JLabel("SAC IV /20")); form.add(sac4); form.add(new JLabel(""));
            form.add(new JLabel("SAC V /20")); form.add(sac5);
            form.add(save); form.add(new JLabel(""));
            p.add(form,BorderLayout.NORTH);

            Runnable loadMarks=()->{
                String r=roll.getText().trim();
                if(r.isEmpty()||subjectBox.getSelectedItem()==null){msg(this,"Enter roll number and select subject.");return;}
                String selected=subjectBox.getSelectedItem().toString();
                String code=selected.substring(0,selected.indexOf(" - "));
                try(Connection c=DB.getConnection()){
                    if(!canEditSubject(c,staffId,code)){
                        msg(this,"Access denied: this staff member is not assigned to "+code+"."); return;
                    }
                    int sid=getStudentId(r), sub=getSubjectId(code);
                    if(sid<0||sub<0){msg(this,"Student or subject not found.");return;}
                    try(PreparedStatement ps=DB.prepare(c,
                        "SELECT cat1,cat2,sac1,sac2,sac3,sac4,sac5 FROM marks WHERE student_id=? AND subject_id=?",sid,sub);
                        ResultSet rs=ps.executeQuery()){
                        if(rs.next()){
                            cat1.setText(String.valueOf(rs.getDouble(1))); cat2.setText(String.valueOf(rs.getDouble(2)));
                            sac1.setText(String.valueOf(rs.getDouble(3))); sac2.setText(String.valueOf(rs.getDouble(4)));
                            sac3.setText(String.valueOf(rs.getDouble(5))); sac4.setText(String.valueOf(rs.getDouble(6))); sac5.setText(String.valueOf(rs.getDouble(7)));
                        }else{ cat1.setText("");cat2.setText("");sac1.setText("");sac2.setText("");sac3.setText("");sac4.setText("");sac5.setText(""); }
                    }
                }catch(Exception ex){msg(this,"Marks load error: "+ex.getMessage());}
            };
            load.addActionListener(e->loadMarks.run());

            save.addActionListener(e->{
                try{
                    String r=roll.getText().trim();
                    if(r.isEmpty()||subjectBox.getSelectedItem()==null){msg(this,"Enter roll number and select subject.");return;}
                    String selected=subjectBox.getSelectedItem().toString();
                    String code=selected.substring(0,selected.indexOf(" - "));
                    try(Connection c=DB.getConnection()){
                        if(!canEditSubject(c,staffId,code)){msg(this,"Access denied: you are not assigned to this subject.");return;}
                    }
                    int sid=getStudentId(r),sub=getSubjectId(code);
                    if(sid<0||sub<0){msg(this,"Student or subject not found.");return;}
                    double a=n(cat1),b=n(cat2),c1=n(sac1),c2=n(sac2),c3=n(sac3),c4=n(sac4),c5=n(sac5);
                    if(a<0||a>100||b<0||b>100||c1<0||c1>20||c2<0||c2>20||c3<0||c3>20||c4<0||c4>20||c5<0||c5>20){
                        msg(this,"Invalid marks. CAT: 0-100, SAC: 0-20.");return;
                    }
                    DB.update("""
                        INSERT INTO marks(student_id,subject_id,cat1,cat2,sac1,sac2,sac3,sac4,sac5)
                        VALUES(?,?,?,?,?,?,?,?,?)
                        ON DUPLICATE KEY UPDATE cat1=VALUES(cat1),cat2=VALUES(cat2),
                        sac1=VALUES(sac1),sac2=VALUES(sac2),sac3=VALUES(sac3),
                        sac4=VALUES(sac4),sac5=VALUES(sac5)
                        """,sid,sub,a,b,c1,c2,c3,c4,c5);
                    msg(this,"Marks saved successfully.");
                }catch(Exception ex){msg(this,"Marks error: "+ex.getMessage());}
            });
            return p;
        }

        JPanel attendancePanel() {
            JPanel p=new JPanel(new BorderLayout(10,10));
            p.setBorder(new EmptyBorder(12,12,12,12));

            JPanel top=card();
            top.setLayout(new FlowLayout(FlowLayout.LEFT,10,10));
            JTextField date=new JTextField(LocalDate.now().toString(),10);
            date.setEditable(false);
            JButton dateBtn=btn("📅 Select Date");
            JComboBox<String> subjectBox=new JComboBox<>();
            JButton load=btn("LOAD STUDENTS");
            JButton save=btn("SAVE ATTENDANCE");

            try(Connection c=DB.getConnection();PreparedStatement ps=c.prepareStatement(
                    "SELECT code,name FROM subjects WHERE semester=3 ORDER BY code");ResultSet rs=ps.executeQuery()){
                while(rs.next())subjectBox.addItem(rs.getString("code")+" - "+rs.getString("name"));
            }catch(SQLException ex){msg(this,"Subject loading error: "+ex.getMessage());}

            top.add(new JLabel("Date"));top.add(date);top.add(dateBtn);
            top.add(new JLabel("Subject Code"));top.add(subjectBox);top.add(load);top.add(save);
            p.add(top,BorderLayout.NORTH);

            String[] columns={"Roll No","Name","Period 1","Period 2","Period 3","Period 4","Period 5","Period 6","Period 7","Period 8"};
            Set<String> validCells=new HashSet<>();
            DefaultTableModel model=new DefaultTableModel(columns,0){
                public boolean isCellEditable(int row,int column){return column>=2 && validCells.contains(row+":"+(column-1));}
            };
            JTable table=new JTable(model);
            table.setRowHeight(30);
            table.getTableHeader().setReorderingAllowed(false);
            JComboBox<String> statusBox=new JComboBox<>(new String[]{"PRESENT","ABSENT","OD"});
            table.setDefaultEditor(Object.class,new DefaultCellEditor(statusBox));
            p.add(new JScrollPane(table),BorderLayout.CENTER);

            dateBtn.addActionListener(e->showCalendar(date));

            load.addActionListener(e->{
                model.setRowCount(0); validCells.clear();
                if(subjectBox.getSelectedItem()==null){msg(this,"Select a subject.");return;}
                String selected=subjectBox.getSelectedItem().toString();
                String code=selected.substring(0,selected.indexOf(" - "));
                try{
                    LocalDate d=LocalDate.parse(date.getText().trim());
                    if(!isStaffAssigned(staffId,code)){msg(this,"Access denied: you are not assigned to "+code+".");return;}
                    int subjectId=getSubjectId(code);
                    String day=d.getDayOfWeek().toString();
                    try(Connection c=DB.getConnection();
                        PreparedStatement ps=DB.prepare(c,"SELECT id,roll_no,name,COALESCE(batch,'I') batch FROM students WHERE department IN ('CSE-A','CSE') ORDER BY roll_no");
                        ResultSet rs=ps.executeQuery()){
                        while(rs.next()){
                            int sid=rs.getInt("id"); String batch=rs.getString("batch");
                            Object[] row=new Object[10]; row[0]=rs.getString("roll_no"); row[1]=rs.getString("name");
                            int tableRow=model.getRowCount();
                            for(int period=1;period<=8;period++){
                                String tq="""
                                    SELECT 1 FROM timetable t
                                    WHERE t.semester=3 AND t.department='CSE' AND t.section='A'
                                      AND t.day_of_week=? AND t.period=? AND t.subject_id=?
                                      AND (t.batch='ALL' OR t.batch=?)
                                    LIMIT 1
                                    """;
                                boolean valid=false;
                                try(PreparedStatement tp=DB.prepare(c,tq,day,period,subjectId,batch);ResultSet tr=tp.executeQuery()){valid=tr.next();}
                                if(valid){
                                    validCells.add(tableRow+":"+period);
                                    String status="ABSENT";
                                    try(PreparedStatement ap=DB.prepare(c,"SELECT status FROM attendance WHERE student_id=? AND subject_id=? AND att_date=? AND period=?",sid,subjectId,java.sql.Date.valueOf(d),period);ResultSet ar=ap.executeQuery()){if(ar.next())status=ar.getString(1);}
                                    row[period+1]=status;
                                }else row[period+1]="—";
                            }
                            model.addRow(row);
                        }
                    }
                    if(model.getRowCount()==0){msg(this,"No students found.");return;}
                    if(validCells.isEmpty()){msg(this,"No timetable slot for "+code+" on "+day+". Attendance is blocked.");return;}
                    msg(this,"Timetable validated. Only scheduled periods are editable.");
                }catch(Exception ex){msg(this,"Attendance load error: "+ex.getMessage());}
            });

            save.addActionListener(e->{
                if(model.getRowCount()==0||validCells.isEmpty()){msg(this,"Load a valid timetable slot first.");return;}
                try{
                    LocalDate d=LocalDate.parse(date.getText().trim());
                    String selected=subjectBox.getSelectedItem().toString();
                    String code=selected.substring(0,selected.indexOf(" - "));
                    if(!isStaffAssigned(staffId,code)){msg(this,"Access denied: you are not assigned to this subject.");return;}
                    int subjectId=getSubjectId(code);
                    for(int row=0;row<model.getRowCount();row++){
                        int sid=getStudentId(model.getValueAt(row,0).toString());
                        for(int period=1;period<=8;period++){
                            if(!validCells.contains(row+":"+period))continue;
                            String status=model.getValueAt(row,period+1).toString();
                            DB.update("""
                                INSERT INTO attendance(student_id,subject_id,att_date,period,status) VALUES(?,?,?,?,?)
                                ON DUPLICATE KEY UPDATE status=VALUES(status)
                                """,sid,subjectId,java.sql.Date.valueOf(d),period,status);
                        }
                    }
                    msg(this,"Attendance saved successfully.");
                }catch(Exception ex){msg(this,"Attendance save error: "+ex.getMessage());}
            });
            return p;
        }

        void showCalendar(JTextField date){
            LocalDate initial;try{initial=LocalDate.parse(date.getText());}catch(Exception e){initial=LocalDate.now();}
            JDialog dialog=new JDialog(this,"Select Date",true);dialog.setSize(430,380);dialog.setLocationRelativeTo(this);dialog.setLayout(new BorderLayout(10,10));
            JPanel header=new JPanel(new BorderLayout());JButton previous=new JButton("◀"),next=new JButton("▶");JLabel monthLabel=new JLabel("",SwingConstants.CENTER);monthLabel.setFont(new Font("Segoe UI",Font.BOLD,16));header.add(previous,BorderLayout.WEST);header.add(monthLabel,BorderLayout.CENTER);header.add(next,BorderLayout.EAST);
            JPanel cal=new JPanel(new GridLayout(0,7,5,5));cal.setBorder(new EmptyBorder(10,10,10,10));final YearMonth[] ym={YearMonth.from(initial)};
            Runnable refresh=()->{cal.removeAll();for(String d:new String[]{"Sun","Mon","Tue","Wed","Thu","Fri","Sat"})cal.add(new JLabel(d,SwingConstants.CENTER));YearMonth m=ym[0];monthLabel.setText(m.getMonth().toString().substring(0,1)+m.getMonth().toString().substring(1).toLowerCase()+" "+m.getYear());int start=m.atDay(1).getDayOfWeek().getValue()%7;for(int i=0;i<start;i++)cal.add(new JLabel(""));for(int day=1;day<=m.lengthOfMonth();day++){JButton b=new JButton(String.valueOf(day));final int dd=day;b.addActionListener(ev->{date.setText(m.atDay(dd).toString());dialog.dispose();});cal.add(b);}cal.revalidate();cal.repaint();};
            previous.addActionListener(e->{ym[0]=ym[0].minusMonths(1);refresh.run();});next.addActionListener(e->{ym[0]=ym[0].plusMonths(1);refresh.run();});dialog.add(header,BorderLayout.NORTH);dialog.add(cal,BorderLayout.CENTER);refresh.run();dialog.setVisible(true);
        }


        JPanel permissionPanel() {
            JPanel p=new JPanel(new BorderLayout(10,10));p.setBorder(new EmptyBorder(12,12,12,12));
            JPanel f=card();f.setLayout(new FlowLayout(FlowLayout.LEFT,10,10));
            JTextField roll=new JTextField(12);JRadioButton allow=new JRadioButton("ALLOW");JRadioButton deny=new JRadioButton("NOT ALLOW");
            ButtonGroup group=new ButtonGroup();group.add(allow);group.add(deny);deny.setSelected(true);
            JButton check=btn("CHECK STATUS"),save=btn("UPDATE PERMISSION");
            f.add(new JLabel("Roll No:"));f.add(roll);f.add(new JLabel("Permission:"));f.add(allow);f.add(deny);f.add(check);f.add(save);p.add(f,BorderLayout.NORTH);
            check.addActionListener(e->{String r=roll.getText().trim();if(r.isEmpty()){msg(this,"Enter roll number.");return;}try(Connection c=DB.getConnection();PreparedStatement ps=DB.prepare(c,"SELECT edit_permission FROM students WHERE roll_no=?",r);ResultSet rs=ps.executeQuery()){if(rs.next()){boolean enabled=rs.getBoolean(1);allow.setSelected(enabled);deny.setSelected(!enabled);msg(this,enabled?"Current status: ALLOW":"Current status: NOT ALLOW");}else msg(this,"Student not found.");}catch(SQLException ex){msg(this,"Permission error: "+ex.getMessage());}});
            save.addActionListener(e->{String r=roll.getText().trim();if(r.isEmpty()){msg(this,"Enter roll number.");return;}try{int n=DB.update("UPDATE students SET edit_permission=? WHERE roll_no=?",allow.isSelected(),r);msg(this,n>0?"Permission updated successfully.":"Student not found.");}catch(SQLException ex){msg(this,"Permission error: "+ex.getMessage());}});
            JTextArea note=new JTextArea("Permission control:\n• ALLOW → student can edit permitted personal details.\n• NOT ALLOW → student profile remains read-only.\n• Marks: STAFF ONLY\n• Attendance: STAFF ONLY\n• Subjects: STAFF ONLY\n• Roll number, department, year and academic records remain protected.");note.setEditable(false);note.setFont(new Font("Segoe UI",Font.PLAIN,15));note.setBorder(new EmptyBorder(10,5,10,5));p.add(note,BorderLayout.CENTER);return p;
        }



        boolean isStaffAssigned(String staffId,String code)throws SQLException{
            try(Connection c=DB.getConnection();PreparedStatement ps=DB.prepare(c,"""
                SELECT 1 FROM staff_subjects ss JOIN staff st ON st.id=ss.staff_id JOIN subjects su ON su.id=ss.subject_id
                WHERE st.staff_id=? AND su.code=? AND COALESCE(st.active,TRUE)=TRUE
                """,staffId,code);ResultSet rs=ps.executeQuery()){return rs.next();}
        }
        boolean canEditSubject(Connection c,String staffId,String code)throws SQLException{
            try(PreparedStatement ps=DB.prepare(c,"SELECT 1 FROM staff_subjects ss JOIN staff st ON st.id=ss.staff_id JOIN subjects su ON su.id=ss.subject_id WHERE st.staff_id=? AND su.code=?",staffId,code);ResultSet rs=ps.executeQuery()){return rs.next();}
        }
        int getStudentId(String roll)throws SQLException{
            try(Connection c=DB.getConnection();PreparedStatement ps=DB.prepare(c,
                    "SELECT id FROM students WHERE roll_no=?",roll);ResultSet rs=ps.executeQuery()){
                return rs.next()?rs.getInt(1):-1;
            }
        }
        int getSubjectId(String code)throws SQLException{
            try(Connection c=DB.getConnection();PreparedStatement ps=DB.prepare(c,
                    "SELECT id FROM subjects WHERE code=?",code);ResultSet rs=ps.executeQuery()){
                return rs.next()?rs.getInt(1):-1;
            }
        }
        double n(JTextField f){return f.getText().isBlank()?0:Double.parseDouble(f.getText().trim());}
    }

    // ---------------- STUDENT ----------------
    static class StudentFrame extends JFrame {
        final String roll, studentName;
        JPanel summary;
        DefaultTableModel marksModel, attModel;
        JLabel perfLabel, attLabel, alertLabel;
        ChartPanel chart;

        StudentFrame(String roll, String name) {
            this.roll=roll;this.studentName=name;
            setTitle("ESEC SIMS - Student Dashboard");
            setSize(1200,760);setLocationRelativeTo(null);setDefaultCloseOperation(EXIT_ON_CLOSE);

            JPanel top=new JPanel(new BorderLayout());
            top.setBackground(LIGHT);top.setBorder(new EmptyBorder(10,15,10,15));
            top.add(title("Student Dashboard"),BorderLayout.WEST);
            top.add(new JLabel("Welcome, "+name+" ("+roll+")"),BorderLayout.EAST);

            JTabbedPane tabs=new JTabbedPane();
            tabs.addTab("Dashboard",dashboard());
            tabs.addTab("Profile",profile());
            tabs.addTab("Marks",marks());
            tabs.addTab("Attendance",attendance());

            JButton logout=btn("Logout");logout.addActionListener(e->{dispose();new LoginFrame();});
            JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT));bottom.add(logout);

            add(top,BorderLayout.NORTH);add(tabs,BorderLayout.CENTER);add(bottom,BorderLayout.SOUTH);setVisible(true);
        }

        JPanel dashboard(){
            JPanel p=new JPanel(new BorderLayout(10,10));p.setBorder(new EmptyBorder(12,12,12,12));
            JPanel cards=new JPanel(new GridLayout(1,3,10,10));
            perfLabel=new JLabel("Performance: --",SwingConstants.CENTER);
            attLabel=new JLabel("Attendance: --",SwingConstants.CENTER);
            alertLabel=new JLabel("Alerts: --",SwingConstants.CENTER);
            for(JLabel l:new JLabel[]{perfLabel,attLabel,alertLabel}){
                l.setFont(new Font("Segoe UI",Font.BOLD,17));JPanel c=card();c.setLayout(new BorderLayout());c.add(l);cards.add(c);
            }
            chart=new ChartPanel();
            summary=card();summary.setLayout(new BorderLayout());
            summary.add(new JLabel("Current Semester: 3",SwingConstants.CENTER),BorderLayout.NORTH);
            summary.add(chart,BorderLayout.CENTER);
            p.add(cards,BorderLayout.NORTH);p.add(summary,BorderLayout.CENTER);
            loadDashboard();
            return p;
        }

        void loadDashboard(){
            try(Connection c=DB.getConnection()){
                int sid=-1;
                try(PreparedStatement ps=DB.prepare(c,"SELECT id FROM students WHERE roll_no=?",roll);
                    ResultSet rs=ps.executeQuery()){if(rs.next())sid=rs.getInt(1);}
                if(sid<0)return;

                double totalP=0,totalT=0, totalPerf=0; int perfCount=0; List<String> names=new ArrayList<>();List<Double> vals=new ArrayList<>();
                String sql="""
                    SELECT s.name,
                           COALESCE((SELECT SUM(a.status='PRESENT')/COUNT(*)*100 FROM attendance a
                                    WHERE a.student_id=? AND a.subject_id=s.id),0) att,
                           COALESCE(m.cat1,0) cat1,COALESCE(m.cat2,0) cat2,
                           COALESCE(m.sac1,0) sac1,COALESCE(m.sac2,0) sac2,
                           COALESCE(m.sac3,0) sac3,COALESCE(m.sac4,0) sac4,COALESCE(m.sac5,0) sac5
                    FROM subjects s
                    LEFT JOIN marks m ON m.subject_id=s.id AND m.student_id=?
                    WHERE s.semester=3 ORDER BY s.code
                    """;
                try(PreparedStatement ps=DB.prepare(c,sql,sid,sid);ResultSet rs=ps.executeQuery()){
                    while(rs.next()){
                        double a=rs.getDouble("att");
                        double perf=assessmentPerformance(rs);
                        names.add(rs.getString("name"));vals.add(perf);
                        if(a>0){totalP+=a;totalT++;}
                        if(perf>0){totalPerf+=perf;perfCount++;}
                    }
                }
                double avgAtt=totalT==0?0:totalP/totalT;
                double avgPerf=perfCount==0?0:totalPerf/perfCount;
                perfLabel.setText("Performance: "+DF.format(avgPerf)+"%");
                attLabel.setText("Attendance: "+DF.format(avgAtt)+"%");
                int alerts=0;
                if(avgAtt<75)alerts++;
                for(double v:vals)if(v>0&&v<50)alerts++;
                alertLabel.setText("Alerts: "+alerts);
                chart.setData(names,vals);
                chart.repaint();
            }catch(Exception ex){System.out.println(ex.getMessage());}
        }

        double assessmentPerformance(ResultSet rs)throws SQLException{
            double sum=0;int count=0;
            double cat1=rs.getDouble("cat1"),cat2=rs.getDouble("cat2");
            double[] sacs={rs.getDouble("sac1"),rs.getDouble("sac2"),rs.getDouble("sac3"),rs.getDouble("sac4"),rs.getDouble("sac5")};
            if(cat1>0){sum+=cat1;count++;}
            if(cat2>0){sum+=cat2;count++;}
            for(double x:sacs)if(x>0){sum+=x*5;count++;} // normalize /20 to percentage
            return count==0?0:sum/count;
        }

        JPanel profile(){
            JPanel p=new JPanel(new BorderLayout(10,10));p.setBorder(new EmptyBorder(12,12,12,12));
            JPanel f=card();f.setLayout(new GridLayout(0,2,8,8));
            JTextField name=new JTextField(),mobile=new JTextField(),email=new JTextField(),address=new JTextField();
            JLabel rollL=new JLabel(),deptL=new JLabel(),yearL=new JLabel();
            try(Connection c=DB.getConnection();PreparedStatement ps=DB.prepare(c,
                    "SELECT name,mobile,email,department,year,address,edit_permission FROM students WHERE roll_no=?",roll);
                ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    name.setText(rs.getString("name"));mobile.setText(rs.getString("mobile"));
                    email.setText(rs.getString("email"));address.setText(rs.getString("address"));
                    rollL.setText(roll);deptL.setText(rs.getString("department"));yearL.setText(String.valueOf(rs.getInt("year")));
                    boolean allow=rs.getBoolean("edit_permission");
                    name.setEditable(allow);mobile.setEditable(allow);email.setEditable(allow);address.setEditable(allow);
                    JButton save=btn("SAVE PERMITTED DETAILS");
                    save.setEnabled(allow);
                    save.addActionListener(e->{
                        try{
                            DB.update("UPDATE students SET name=?,mobile=?,email=?,address=? WHERE roll_no=?",
                                    name.getText(),mobile.getText(),email.getText(),address.getText(),roll);
                            msg(this,"Profile updated.");
                        }catch(SQLException ex){msg(this,"Update error: "+ex.getMessage());}
                    });
                    f.add(new JLabel("Roll Number (Protected)"));f.add(rollL);
                    f.add(new JLabel("Department (Protected)"));f.add(deptL);
                    f.add(new JLabel("Year (Protected)"));f.add(yearL);
                    f.add(new JLabel("Name"));f.add(name);
                    f.add(new JLabel("Mobile"));f.add(mobile);
                    f.add(new JLabel("Email"));f.add(email);
                    f.add(new JLabel("Address"));f.add(address);
                    f.add(new JLabel(""));f.add(save);
                }
            }catch(SQLException ex){msg(this,"Profile load error: "+ex.getMessage());}
            p.add(f,BorderLayout.NORTH);return p;
        }

        JPanel marks(){
            JPanel p=new JPanel(new BorderLayout());p.setBorder(new EmptyBorder(12,12,12,12));
            String[] cols={"Code","Subject","CAT I","CAT II","SAC I","SAC II","SAC III","SAC IV","SAC V","Performance %"};
            marksModel=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            JTable t=new JTable(marksModel);loadMarks();
            p.add(new JScrollPane(t),BorderLayout.CENTER);return p;
        }

        void loadMarks(){
            marksModel.setRowCount(0);
            try(Connection c=DB.getConnection()){
                String sql="""
                    SELECT s.code,s.name,COALESCE(m.cat1,0),COALESCE(m.cat2,0),
                    COALESCE(m.sac1,0),COALESCE(m.sac2,0),COALESCE(m.sac3,0),
                    COALESCE(m.sac4,0),COALESCE(m.sac5,0)
                    FROM subjects s LEFT JOIN marks m ON m.subject_id=s.id
                    AND m.student_id=(SELECT id FROM students WHERE roll_no=?)
                    WHERE s.semester=3 ORDER BY s.code
                    """;
                try(PreparedStatement ps=DB.prepare(c,sql,roll);ResultSet rs=ps.executeQuery()){
                    while(rs.next()){
                        double perf=0;int count=0;
                        double cat1=rs.getDouble(3),cat2=rs.getDouble(4);
                        if(cat1>0){perf+=cat1;count++;}if(cat2>0){perf+=cat2;count++;}
                        for(int i=5;i<=9;i++){double x=rs.getDouble(i);if(x>0){perf+=x*5;count++;}}
                        perf=count==0?0:perf/count;
                        marksModel.addRow(new Object[]{rs.getString(1),rs.getString(2),cat1,cat2,
                                rs.getDouble(5),rs.getDouble(6),rs.getDouble(7),rs.getDouble(8),rs.getDouble(9),DF.format(perf)});
                    }
                }
            }catch(SQLException ex){msg(this,"Marks load error: "+ex.getMessage());}
        }

        JPanel attendance(){
            JPanel p=new JPanel(new BorderLayout());p.setBorder(new EmptyBorder(12,12,12,12));
            String[] cols={"Code","Subject","Total Periods","Present","Absent","Attendance %"};
            attModel=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            JTable t=new JTable(attModel);loadAttendance();
            p.add(new JScrollPane(t),BorderLayout.CENTER);return p;
        }

        void loadAttendance(){
            attModel.setRowCount(0);
            try(Connection c=DB.getConnection()){
                String sql="""
                    SELECT s.code,s.name,
                    COUNT(a.id) total,
                    SUM(a.status='PRESENT') present,
                    SUM(a.status='ABSENT') absent
                    FROM subjects s LEFT JOIN attendance a ON a.subject_id=s.id
                    AND a.student_id=(SELECT id FROM students WHERE roll_no=?)
                    WHERE s.semester=3 GROUP BY s.id,s.code,s.name ORDER BY s.code
                    """;
                try(PreparedStatement ps=DB.prepare(c,sql,roll);ResultSet rs=ps.executeQuery()){
                    while(rs.next()){
                        int total=rs.getInt(3),present=rs.getInt(4),absent=rs.getInt(5);
                        double pct=total==0?0:present*100.0/total;
                        attModel.addRow(new Object[]{rs.getString(1),rs.getString(2),total,present,absent,DF.format(pct)});
                    }
                }
            }catch(SQLException ex){msg(this,"Attendance load error: "+ex.getMessage());}
        }
    }

    // ---------------- PIE CHART ----------------
    static class ChartPanel extends JPanel {
        List<String> names=new ArrayList<>();List<Double> vals=new ArrayList<>();
        void setData(List<String> n,List<Double> v){names=new ArrayList<>(n);vals=new ArrayList<>(v);}
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g;g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            if(vals.isEmpty()){g2.drawString("No performance data yet.",40,50);return;}
            int x=40,y=30,size=Math.min(360,getHeight()-70);
            double total=0;for(double v:vals)total+=Math.max(v,0.1);
            double start=0;
            Color[] colors={new Color(230,126,34),new Color(52,152,219),new Color(46,204,113),
                    new Color(155,89,182),new Color(241,196,15),new Color(231,76,60),
                    new Color(26,188,156),new Color(127,140,141),new Color(52,73,94),new Color(149,165,166)};
            for(int i=0;i<vals.size();i++){
                double extent=360*Math.max(vals.get(i),0.1)/total;
                g2.setColor(colors[i%colors.length]);
                g2.fill(new Arc2D.Double(x,y,size,size,start,extent,Arc2D.PIE));
                start+=extent;
            }
            int ly=40;
            g2.setFont(new Font("Segoe UI",Font.PLAIN,13));
            for(int i=0;i<names.size() && i<10;i++){
                g2.setColor(colors[i%colors.length]);g2.fillRect(440,ly-12,14,14);
                g2.setColor(Color.DARK_GRAY);
                String s=names.get(i)+" : "+DF.format(vals.get(i))+"%";
                if(s.length()>38)s=s.substring(0,38)+"...";
                g2.drawString(s,462,ly);
                ly+=27;
            }
            g2.setColor(Color.DARK_GRAY);g2.drawString("Subject-wise performance",40,y+size+35);
        }
    }
}
