set j=c:\Program Files\Java\jdk1.7.0_11\bin\
set cp=lib\gson-2.2.3.jar
"%j%javac.exe" -cp %cp% *.java 
if not errorlevel 1 "%j%java.exe" -cp .;%cp% Faltung1
