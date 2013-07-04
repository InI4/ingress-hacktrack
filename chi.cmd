set j=c:\Program Files\Java\jdk1.7.0_11\bin\
"%j%javac.exe" -cp %cp% -d classes src/*.java 
if not errorlevel 1 "%j%java.exe" -cp classes de.spieleck.ingress.hackstat.SFunc 
