set j=c:\Program Files\Java\jdk1.7.0_45\bin\
set cp=lib\saxon.jar;lib\gson-2.2.3.jar;lib\log4j-1.2.17.jar;lib\trove-3.0.3.jar
"%j%javac.exe" -cp %cp% -d classes src/*.java 
if not errorlevel 1 "%j%java.exe" -cp classes;%cp%;. de.spieleck.ingress.hackstat.Phase1 private/_all_docs.js
