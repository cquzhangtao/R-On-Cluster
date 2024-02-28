# Introduction
How to train a model with different datasets and parameters in parallel on a cluster? This project gives a very simple solution. Write your script first and replace the parameters that appeared in the script  with [[name of a parameter]]. The script turns into a template. Then, write all the possible scenarios with the value of parameters into a scenario file. Pass the template and the scenario files to the tool, all the scenarios with be executed in parallel on the cluster.

# New instruction 
1) make sure Java, R, and Rserve are installed and up to date
2) copy the tool folder on your machine.
The folder has the following contents, <br/>

----lib                 //please put all required libraries in this folder  <br/>
----installed_lib       // The libraries in the lib folder will be installed under this folder  <br/>
----tool.jar            // This is a runnable jar file. All codes are inside.  <br/>
----tool_lib            // This includes the R-related jar files needed by the tool  <br/>
----run.bat             // on Windows click this file to run the tool  <br/>
----config.txt          // the config file  <br/>
----test                //This folder contains a simple scenario file and a simple template file, which can be used to test the installation.  <br/>

3) modify the config file
please set installation paths of R and Rserve, and the user working path.

4) create the scenario file
In the scenario file, please add the names of all parameters in the first line and separate them with a tab. 
Please also add the following headers: Include, Name, and Template. 
The column "Template" refers to the locations of templates used by the scenarios. 

If the column Include is set to "y" or "Y", the scenario will be included. For other letters, not included. Please do not use white space here.

Please add new lines to define scenarios. The values are also separated with a tab. Empty lines are allowed.

!!!!The name of the scenario file is fixed to "scenarios.txt" and please put it under the user working folder.
!!!!Please keep the names of parameters in the template file and the scenarios.txt file the same. The names of parameters are case insensitive. The order of the headers does not matter. 


!!NEW!! "ScenarioPath" is a built-in parameter. You do not need to define it. It refers to the location: User_working_path/Scenario_Name/.
!!NEW!! Column "Template" is optional. If you do not add this column, All scenarios will use the template.txt under the user working folder.
!!NEW!! If the parameter name starts with *, we will consider the values as file paths. The corresponding files will be copied to the scenario folders: 
!!NEW!! If the input data files do not exist, the file path will be considered to be the relative path starting from the user's working path
!!NEW!! The generated scripts will be also put in the scenario folders.

!!ATTENTION!!
!!PUT scenarios.txt IN THE USER WORKING FOLDER.

5) create the template file

please use "[[name of a parameter]]" to replace the values you want to configure later. There is no requirement for the names of parameters.
If all scenarios use the same template, you can put the template under the user working folder without specifying it in the scenario file.

!!ATTENTION!! For the names starting with "*" in the scenario file, please do not write "*" in the templates.

6) 	test the installation

a. set the user working path in config.txt to the test folder (full path)

b. execute a command to run the jar file

$ java -jar tool.jar

c. if everything goes well, in the test folder, 5 scenario folders will be created and each one contains a script file, a data file, and a result file.


#  Old Instruction

A. Installation 

In Windows, please follow the steps below to set up this tool.

1) Install Java if not installed yet

2) Install R if not installed

3) Install rJava in R 
install.packages("rJava")

4) Change the paths of Java, R, and rJava in run.bat

5) Click run.bat to run the tool.


In Debian, please follow the steps below to set up this tool.

1) Install Java Development Toolkit (JDK) if not installed yet

$apt-get install default-JDK

2) Install R if not installed 

$apt-get install r-base

//If you install R before JDK, please reconfig java for R
$R CMD javareconf -e

3) Run R

$R

4) Install rJava in R 

> install.packages("rJava")
> q()

6) Modify .bashrc file

$gedit .bashrc

Add the following two lines in the file, which specify respectively where is R and rJava installed. Change the string according to your situation.

export LD_LIBRARY_PATH=/usr/lib/R/bin/:/usr/local/lib/R/site-library/rJava/jri/
export R_HOME=/usr/lib/R/

$source .bashrc  //refresh or just restart the terminal


7) Run the tool
$ java -jar /some/where/you/copy/to/OsramRClient.jar


8) Done

9) If you changed the configuration and want to run it again, please just input the command,

$ java -jar /some/where/you/copy/to/OsramRClient.jar


B. Configuration

Two files are required, a Template file and a scenario file.
In the template file, please use "[[name of a parameter]]" to replace the values you want to configure later. There is no requirement for the name and location of template files.
In the scenario file, please add the names of all parameters to the first line and separate them with a tab. Please also add the following headers, Include, Name, and Template. Column "Template" refers to the location of the templates.
Please add new lines to define scenarios. The values are also separated with a tab. Empty lines are allowed.

!!!!The name of the scenario file is fixed to "scenarios.txt" and please put it under the tool folder.
!!!!Please keep the names of parameters in the template file and the scenarios.txt file the same. The names of parameters are case insensitive. The order of the headers does not matter. 

C. Library

If you want to use other libraries, please download them and put them into the lib folder in the tool.
For Windows, download the zip file; For Linux, download the .tar.gz file.

D. Rserve installation

Manually installation
1) download Rserve package
2) in R run install.packages("package location",,repos=NULL, type="source")
//if this error occurs "'configure' exists but is not executable -- see the 'R Installation and Administration Manual'"
// please run $mkdir new_folder $export TMPDIR= path_of_new_folder first

Run Rserve in a user path
1) .libPaths("Rserve_installed_path")
2) library("Rserve")
3) Rserve()



