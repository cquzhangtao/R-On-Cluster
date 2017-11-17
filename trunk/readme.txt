
Please follow steps below to setup this tool on Debian.

1) Install Java Development Toolkit (JDK)

$apt-get install default-jdk

2) Install R

$apt-get install r-base

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


