==============================================================
 DRUG INFORMATION ANALYSIS AND SIDE-EFFECT SEARCH SYSTEM
==============================================================

WHAT IT DOES
A website where you type a medicine name (like Dolo 650) or a chemical
name (like Paracetamol) and your age. It shows:
  - the drugs inside the medicine
  - how safe it is for that age (child, adult, senior)
  - how much is safe
  - how to use it
  - side effects
It also has a Reports page (analysis) and an Admin page (add, edit,
delete medicines).

Built with: Java 17 + Spring Boot (backend), MySQL (database),
HTML + CSS + JavaScript (website).


--------------------------------------------------------------
 WHAT YOU NEED (install once, all free)
--------------------------------------------------------------
1. Java JDK 17
   Search "Temurin JDK 17" and install it.
   Check: open Command Prompt and type   java -version
   It should show 17.

2. MySQL Server + MySQL Workbench
   Download from dev.mysql.com/downloads (choose "Custom" and pick
   only MySQL Server and MySQL Workbench).
   Remember the ROOT PASSWORD you set. You need it below.

3. VS Code (code.visualstudio.com)
   Then open Extensions inside VS Code and install
   "Extension Pack for Java".


--------------------------------------------------------------
 HOW TO RUN (5 steps)
--------------------------------------------------------------
STEP 1: Unzip this folder (right-click the zip > Extract All).

STEP 2: Create the database
  a) Open MySQL Workbench and double-click your connection.
  b) Click File > Open SQL Script and choose:
        database/drugdb.sql
  c) Click the lightning bolt button to run it.
  d) On the left, click the refresh icon next to "Schemas".
     You should see "drugdb" with 7 tables.

STEP 3: Put YOUR MySQL password
  a) Open this file in any editor:
        backend/src/main/resources/application.properties
  b) On the line  spring.datasource.password=YOUR_MYSQL_PASSWORD
     replace YOUR_MYSQL_PASSWORD with your own root password.
     (No spaces, no quotes.)
  c) Save the file.

STEP 4: Start the backend
  a) Open VS Code. Click File > Open Folder and choose the folder
     called "backend" (the one that has pom.xml directly inside).
     If VS Code asks "Do you trust the authors", click Yes.
  b) Wait until the bottom bar says "Java: Ready".
  c) Open src/main/java/com/drugproject/DrugApplication.java
  d) Click "Run" (small text above "public static void main").
     The first time it downloads files, so keep the internet on
     and wait a few minutes.
  e) When the terminal shows "Started DrugApplication", it is running.

STEP 5: Open the website
  In your browser, go to:   http://localhost:8080

Keep VS Code open while using the website. If you close it, the
website stops.


--------------------------------------------------------------
 HOW TO USE THE WEBSITE
--------------------------------------------------------------
Medicine Checker (home page)
  1. Type a medicine or chemical name (or click one of the buttons).
  2. Type the age.
  3. Press Check.
  Try:  Paracetamol + age 30  (shows 3 medicines, click one)
        Disprin + age 8       (shows a red "Avoid" for a child)
        Combiflam + age 70    (shows "Be careful")

Reports page
  Shows totals, most common side effects, medicines with the most
  side effects, medicines per chemical, and safety by age group.

Admin page
  Add, edit or delete medicines.
  The "Admin key" box: leave it empty when running on your own laptop.
  (It is only used when the website is put online with an ADMIN_KEY.)


--------------------------------------------------------------
 COMMON PROBLEMS
--------------------------------------------------------------
"Port 8080 was already in use"
  The backend is already running somewhere. Click the red square
  (stop) in VS Code, wait 5 seconds, then click Run once.
  Or in Command Prompt:
      netstat -ano | findstr :8080
      taskkill /PID <the number at the end> /F

"Access denied for user 'root'"
  The password in application.properties is wrong.

"Unknown database 'drugdb'"
  You have not run database/drugdb.sql yet (Step 2).

"Failed to configure a DataSource"
  application.properties is missing or in the wrong folder. It must be
  at backend/src/main/resources/application.properties

Run button not showing
  Wait for "Java: Ready", check that JDK 17 and "Extension Pack for
  Java" are installed, and make sure you opened the "backend" folder.

Page not opening at localhost:8080
  The backend is not running. Start it again (Step 4).

Using XAMPP instead of MySQL Server?
  Start MySQL in XAMPP, open phpMyAdmin > Import > choose
  database/drugdb.sql. XAMPP's root password is usually empty, so write
  spring.datasource.password=   (nothing after the equals sign).
  Do not run XAMPP MySQL and MySQL Server together (same port 3306).


--------------------------------------------------------------
 PROJECT STRUCTURE
--------------------------------------------------------------
drug-project/
  README.txt
  database/
    drugdb.sql                 tables + sample data (7 medicines)
  backend/
    pom.xml                    project settings
    src/main/java/com/drugproject/
      DrugApplication.java     starts the backend
      MedicineController.java  search links
      MedicineService.java     search logic
      MedicineRepository.java  database questions (SQL)
      AdminController/Service/Repository   add, edit, delete medicines
      ReportController/Repository          analysis reports
      AdminKeyFilter.java      optional admin key
    src/main/resources/
      application.properties   database password goes here
      static/                  the website (HTML, CSS, JavaScript)


--------------------------------------------------------------
 MODULES (for the group)
--------------------------------------------------------------
1. Database        : drugdb.sql
2. Backend search  : Medicine controller, service, repository
3. Frontend        : static/ (index.html, style.css, script.js)
4. Admin + Reports : Admin and Report files, admin.html, reports.html
5. Testing, report and PPT: everyone

DATABASE TABLES
  medicines, ingredients, medicine_ingredients, side_effects,
  medicine_side_effects, age_rules, usage_steps

TEST LINKS (backend must be running)
  http://localhost:8080/api/medicine?name=Disprin&age=8
  http://localhost:8080/api/medicine?name=Paracetamol&age=30
  http://localhost:8080/api/suggestions
  http://localhost:8080/api/reports

FUTURE IMPROVEMENTS
  - Admin login (the optional admin key is a first step)
  - More medicines and more age rules
  - Put the website online


--------------------------------------------------------------
 SHARING ON GITHUB
--------------------------------------------------------------
- Upload this whole "drug-project" folder to a GitHub repository.
- Friends can click the green "Code" button > "Download ZIP" (or use
  git clone), then follow the steps above.
- IMPORTANT: application.properties in this folder only has the
  placeholder YOUR_MYSQL_PASSWORD. Put your real password in it only
  on your own laptop. Never upload your real password to GitHub.
- GitHub only stores the code. It does not run the website. To run it,
  follow the 5 steps above.

--------------------------------------------------------------
 IMPORTANT
--------------------------------------------------------------
This project is for learning only. The medicine data is sample data
and is NOT medical advice. Always follow the medicine label and your
doctor's advice.
