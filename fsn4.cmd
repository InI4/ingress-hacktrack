set j=c:\Program Files\Java\jdk1.7.0_11\bin\
"%j%javac.exe" -d classes src/Grundmass*.java src/GOpt1D.java
if not errorlevel 1 "%j%java.exe" -cp classes de.spieleck.ingress.hackstat.Grundmass %* > out.txt
sed "s/,/\\./g" out.txt > out2.txt
