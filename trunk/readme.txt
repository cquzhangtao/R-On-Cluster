
A. Installation 

In windows, please follow steps below to setup this tool.

1) Install Java if not installed yet

2) Install R if not installed

3) Install rJava in R 
install.packages("rJava")

4) Change the paths of Java, R and rJava in run.bat

5) Click run.bat to run the tool.


In Debian, please follow steps below to setup this tool .

1) Install Java Development Toolkit (JDK) if not installed yet

$apt-get install default-jdk

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

9) If you changed the configuration and want to run again, please just input command,

$ java -jar /some/where/you/copy/to/OsramRClient.jar


B. Configuration

Two files are required, Template file and scenario file.
In the template file, please use "[[name of a parameter]]" to replace the values you want to config later. There is no requirement on the name and location of template files.
In the scenario file, please add names of all parameters to the first line and separate with a tab. Please also add the following headers, Include, Name, and Template. Column "Template" refers to the location of the templates.
Please add new lines to define scenarios. The values are also separated with a tab. Empty lines are allowed.

!!Please keep the names in the template file and the scenario file the same. The order of the headers does not matter. The names of parameters are case insensitive.

C. Library

If you want to use other libraries, please download them and put them into the lib folder in the tool.
For windows, download zip file; For Linux, download .tar.gz file.

