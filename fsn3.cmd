set j=c:\Program Files\Java\jdk1.7.0_11\bin\
"%j%javac.exe" -d classes src/Stats*.java 
if not errorlevel 1 "%j%java.exe" -cp classes de.spieleck.ingress.hackstat.Stats2D
if not errorlevel 1 "%j%java.exe" -cp classes de.spieleck.ingress.hackstat.Stats1D
