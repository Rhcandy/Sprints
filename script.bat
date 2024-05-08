

REM Define variables
SET "APP_NAME=Sprint_prom16"
SET "APP_DIR=%~dp0"
SET "PACKAGE_DIR=%APP_DIR%bin"
SET "JAR_FILE=%APP_DIR%%APP_NAME%.jar"
SET "LIB_DIR=D:\Mr naina\framework\sprints\lib"
SET "TEMP_DIR=%APP_DIR%temp"

CD /D "%APP_DIR%"
javac -cp "./lib/servlet-api.jar" -d "./bin" "./src/mgituprom16/*.java"

REM Create TEMP_DIR if it doesn't exist
MKDIR "%TEMP_DIR%"

REM Copy PACKAGE_DIR content to TEMP_DIR
XCOPY "%PACKAGE_DIR%\*" "%TEMP_DIR%\"  /E /I /Y

REM Create a .jar file from the temporary directory
jar cvf "%JAR_FILE%" -C "%TEMP_DIR%" .

REM Copy the JAR_FILE into the LIB_DIR directory
COPY /Y "%JAR_FILE%" "%LIB_DIR%"

REM Remove TEMP_DIR and its content
RD /S /Q "%TEMP_DIR%"
@REM ECHO Creation of the application "%APP_NAME%" completed successfully!

